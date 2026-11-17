package tech.ula.ui

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import tech.ula.R
import tech.ula.databinding.FragAppDetailsBinding
import tech.ula.model.repositories.UlaDatabase
import tech.ula.utils.* // ktlint-disable no-wildcard-imports
import tech.ula.viewmodel.AppDetailsEvent
import tech.ula.viewmodel.AppDetailsViewModel
import tech.ula.viewmodel.AppDetailsViewState
import tech.ula.viewmodel.AppDetailsViewmodelFactory

class AppDetailsFragment : Fragment() {

    private var _binding: FragAppDetailsBinding? = null
    private val binding get() = requireNotNull(_binding) { "View binding accessed outside view lifecycle" }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private lateinit var activityContext: Activity

    private val args: AppDetailsFragmentArgs by navArgs()
    private val app by lazy { args.app!! }

    private val viewModel by lazy {
        val sessionDao = UlaDatabase.getInstance(activityContext).sessionDao()
        val appDetails = AppDetails(activityContext.filesDir.path, activityContext.resources)
        val buildVersion = Build.VERSION.SDK_INT
        val factory = AppDetailsViewmodelFactory(sessionDao, appDetails, buildVersion, activityContext.getSharedPreferences("apps", Context.MODE_PRIVATE))
        ViewModelProviders.of(this, factory)
                .get(AppDetailsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragAppDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activityContext = activity!!
        viewModel.viewState.observe(this, Observer<AppDetailsViewState> { viewState ->
            viewState?.let {
                handleViewStateChange(viewState)
            }
        })
        viewModel.submitEvent(AppDetailsEvent.SubmitApp(app))
        setupPreferredServiceTypeRadioGroup()
        setupAutoStartCheckbox()
    }

    private fun handleViewStateChange(viewState: AppDetailsViewState) {
        binding.appsIcon.setImageURI(viewState.appIconUri)
        binding.appsTitle.text = viewState.appTitle
        binding.appsDescription.text = viewState.appDescription
        handleEnableRadioButtons(viewState)
        handleShowStateHint(viewState)

        if (viewState.selectedServiceTypeButton != null) {
            binding.appsServiceTypePreferences.check(viewState.selectedServiceTypeButton)
        }

        binding.checkboxAutoStart.setChecked(viewState.autoStartEnabled)
    }

    private fun handleEnableRadioButtons(viewState: AppDetailsViewState) {
        binding.appsSshPreference.isEnabled = viewState.sshEnabled
        binding.appsVncPreference.isEnabled = viewState.vncEnabled

        if (viewState.xsdlEnabled) {
            binding.appsXsdlPreference.isEnabled = true
        } else {
            // Xsdl is unavailable on Android 9 and greater
            binding.appsXsdlPreference.isEnabled = false
            binding.appsXsdlPreference.alpha = 0.5f

            val xsdlSupportedText = view?.find<TextView>(R.id.text_xsdl_version_supported_description)
            xsdlSupportedText?.visibility = View.VISIBLE
        }
    }

    private fun handleShowStateHint(viewState: AppDetailsViewState) {
        if (viewState.describeStateHintEnabled) {
            binding.textDescribeState.visibility = View.VISIBLE
            binding.textDescribeState.setText(viewState.describeStateText!!)
        } else {
            binding.textDescribeState.visibility = View.GONE
        }
    }

    private fun setupPreferredServiceTypeRadioGroup() {
        binding.appsServiceTypePreferences.setOnCheckedChangeListener { _, checkedId ->
            viewModel.submitEvent(AppDetailsEvent.ServiceTypeChanged(checkedId, app))
        }
    }

    private fun setupAutoStartCheckbox() {
        binding.checkboxAutoStart.setOnCheckedChangeListener { _, checked ->
            viewModel.submitEvent(AppDetailsEvent.AutoStartChanged(checked, app))
        }
    }
}