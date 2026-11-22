#!/usr/bin/env python3
"""Read-only audit of pinned legacy Android build inputs.

This does not compile the APK or claim Maven artifacts are available.
It distinguishes a reproducible *source checkout* from a reproducible build.
"""
import json
import pathlib
import re
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]


def read(path):
    file = ROOT / path
    if not file.is_file():
        return None
    return file.read_text(encoding="utf-8", errors="replace")


def contains(path, pattern):
    source = read(path)
    return source is not None and bool(re.search(pattern, source))


def submodule_commit(path):
    try:
        result = subprocess.run(
            ["git", "-C", str(ROOT), "ls-tree", "HEAD", path],
            capture_output=True, text=True, check=True,
        ).stdout.split()
        return result[2] if len(result) >= 4 and result[1] == "commit" else None
    except (OSError, subprocess.CalledProcessError):
        return None


def main():
    checks = {
        "legacy_gradle_6_5": contains(
            "gradle/wrapper/gradle-wrapper.properties",
            r"gradle-6\.5-(?:bin|all)\.zip",
        ),
        "android_gradle_plugin_4_1_1": contains(
            "build.gradle", r"android_plugin\s*=\s*['\"]4\.1\.1['\"]",
        ),
        "android_compile_sdk_30": contains(
            "gradle.properties", r"(?m)^compileSdkVersion=30$",
        ),
        "android_min_sdk_21": contains(
            "gradle.properties", r"(?m)^minSdkVersion=21$",
        ),
        "legacy_freerdp_project_reference": contains(
            "remote-desktop-clients/bVNC/build.gradle",
            r"freeRDPCore",
        ),
        "legacy_sshlib_2_2_13_reference": contains(
            "remote-desktop-clients/pubkeyGenerator/build.gradle",
            r"org\.connectbot:sshlib:2\.2\.13",
        ),
        "legacy_termux_bintray_reference": contains(
            "termux-app/terminal-emulator/build.gradle",
            r"com\.jfrog\.bintray",
        ),
        "production_downloader_uses_shared_parser": (
            (read("app/src/main/java/tech/ula/utils/AssetDownloader.kt") or "")
            .count("DownloadNameParser.parse(") == 2
        ),
    }
    pins = {
        "remote_desktop_clients": submodule_commit("remote-desktop-clients"),
        "termux_app": submodule_commit("termux-app"),
    }
    expected_pins = {
        "remote_desktop_clients": "0047fd1a35c50fe00de73f97b167ec68c8e12347",
        "termux_app": "d6dcbd5d7b0c03f48db7827d10d072d7f240b832",
    }
    checks["submodules_match_audited_pins"] = pins == expected_pins
    blockers = []
    if not all(checks.values()):
        blockers.append("Source checkout differs from audited baseline or nested submodules are missing")
    if checks["legacy_sshlib_2_2_13_reference"]:
        blockers.append(
            "org.connectbot:sshlib:2.2.13 unresolved in CI run 45046826041; "
            "pin a verified source/mirror or verify a compatible replacement"
        )
    if checks["legacy_termux_bintray_reference"]:
        blockers.append(
            "Legacy Termux Bintray publishing plugins need CI-only removal"
        )
    blockers.append(
        "FreeRDP Android project is generated from pinned upstream source in CI; "
        "not materialized in the repository Git tree"
    )
    report = {
        "status": "BASELINE_RECORDED_BUILD_NOT_VERIFIED",
        "checks": checks,
        "submodule_commits": pins,
        "expected_submodule_commits": expected_pins,
        "blocking_conditions": blockers,
        "tested_java_for_legacy_gradle": "11",
        "tested_android_sdk": "30",
        "tested_build_tools": "30.0.3",
        "gradle_configuration_passed_run": 45046826041,
        "gradle_test_status": "NOT_EXECUTED",
    }
    print(json.dumps(report, indent=2, sort_keys=True))
    return 0 if all(checks.values()) else 2


if __name__ == "__main__":
    sys.exit(main())
