# cameraProject
##What does my app do?

The idea of this app is to enable your phone to act as a temporary security camera, available everywhere on the fly.
It captures an image when movement is detected in the camera FOV.

The application uses CameraX and its ImageAnalysis and ImageCapture features. Currently it senses major movement trough luminosity analysis. 
That is donewith comparing the average luminosity values with the previous ones and calculating the differences. Big enough changes trigger the ImageCapture.
