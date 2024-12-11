package com.example.skincure.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Outline
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentCameraBinding
import com.example.skincure.utils.createCustomTempFile
import androidx.navigation.fragment.findNavController
import com.example.skincure.utils.showToast

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null
    private val TAG = "CameraFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
    }

    override fun onResume() {
        super.onResume()
        checkPermissionsAndStartCamera()
    }

    private fun setupView() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val previewView: PreviewView = binding.viewFinder
        previewView.outlineProvider = RoundedOutlineProvider(cornerRadius = 50f)
        previewView.clipToOutline = true

        binding.galleryLayout.setOnClickListener { startGallery() }
        binding.captureButton.setOnClickListener { takePhoto() }
        binding.switchLayout.setOnClickListener { switchCamera() }
    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            showToast(requireContext(), getString(R.string.photo_selected))
            val bundle = Bundle().apply {
                putString(EXTRA_CAMERAX_IMAGE, uri.toString())
            }
            findNavController().navigate(R.id.action_camera_to_resultDetailFragment, bundle)
        } else {
            showToast(requireContext(), getString(R.string.photo_failed))
        }
    }

    private fun checkPermissionsAndStartCamera() {
        if (allPermissionsGranted()) {
            startCamera()
        } else if (shouldShowRequestPermissionRationale(REQUIRED_PERMISSION)) {
            showToast(
                requireContext(),
                getString(R.string.permission_request_granted))
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }
    }


    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast(requireContext(), getString(R.string.permission_request_granted))
                startCamera()
            } else {
                showToast(requireContext(), getString(R.string.permission_request_denied))
            }
        }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (exc: Exception) {
                showToast(
                    requireContext(),
                    getString(R.string.failed_open_camera),
                )
                Log.e(TAG, "startCamera: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = createCustomTempFile(requireContext())

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    val bundle = Bundle().apply {
                        putString(EXTRA_CAMERAX_IMAGE, savedUri.toString())
                    }
                    if (findNavController().currentDestination?.id == R.id.camera) {
                        findNavController().navigate(R.id.action_camera_to_resultDetailFragment, bundle)
                    }
                    showToast(requireContext(), getString(R.string.image_captured))
                }

                override fun onError(exc: ImageCaptureException) {
                    showToast(requireContext(), getString(R.string.capture_error))
                    Log.e(TAG, "onError: ${exc.message}")
                }
            }
        )
    }

    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture?.targetRotation = rotation
            }
        }
    }

    class RoundedOutlineProvider(private val cornerRadius: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
        }
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
    }
}