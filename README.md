# Hue Harvester

[![Italiano](https://img.shields.io/badge/lang-italiano-green.svg)](README.it.md)

by Tommaso Bellinato | [2032597](https://stem.elearning.unipd.it/user/profile.php?id=3804)

Development of the **"Project C"** for the “Elements of Embedded Systems Programming” course (6 ECTS, code INP8085258) 2023-24 edition.

## Project Specifications

The task is to implement an app that provides the average color detected by the camera.

- The first interface of the app must show
    1. the camera preview and
    2. the average value calculated for each of the three color components
    3. with real-time updates
- The second interface must show, for each of the three color components
    1. the average values of the last five minutes
    2. graphically
    3. with real-time updates
- It is not required for the calculation and recording of data to continue when the app is in the background, but in case the app goes into the background
    1. the app itself must not crash and must remain in a consistent state.

Images can be acquired using the [`Camera`](https://developer.android.com/guide/topics/media/camera) class even though it is deprecated.

- Whatever the acquisition method used, it is recommended to
    1. set the [size](https://developer.android.com/reference/kotlin/android/hardware/Camera.Parameters#getsupportedpreviewsizes) and [frame rate](https://developer.android.com/reference/kotlin/android/hardware/Camera.Parameters#getsupportedpreviewfpsrange) of the preview to the minimum values supported by the device's camera.

External libraries can be used to implement data visualization.

## How the App Was Implemented

### Main Features

1. **Camera Preview**:
    - Real-time display of the camera preview.
2. **Average Color Value Calculation**:
    - Real-time calculation of the average RGB values for each captured frame.
    - [**OpenCV**](https://opencv.org/): A library used for image processing that can be used to optimize the average color values.
3. **Graphical Display of Average Values from the Last Five Minutes**:
    - Real-time graph showing the trend of color components from the last five minutes of acquisition
    - [**MPAndroidChart**](https://github.com/PhilJay/MPAndroidChart): A comprehensive library for creating real-time charts on Android. Easy to use and highly customizable.

### Secondary Features

1. **Language Support**
    - The application fully supports both English (default) and Italian languages.
2. **Real-Time Interface Updates**:
    - Continuous and smooth updates of values and charts during app usage.
    - [**LiveData**](https://developer.android.com/topic/libraries/architecture/livedata) and [**ViewModel**](https://developer.android.com/topic/libraries/architecture/viewmodel): for managing and updating data reactively and efficiently.
    - [**Room**](https://developer.android.com/kotlin/multiplatform/room): for storing 5 minutes of data detection.
3. **UI Functionality in Both Portrait and Landscape Modes**:
    - Adaptability of the user interface to work correctly in both device orientations.
    - [**ConstraintLayout**](https://developer.android.com/training/constraint-layout): A versatile layout manager that facilitates the creation of responsive and adaptable UIs for different screen sizes and orientations.
4. **Consistent App State in Background**:
    - Management of the app lifecycle to ensure it does not crash and remains in a consistent state when going into the background.
5. **Setting Preview Size and Frame Rate to Minimum Supported Values**:
    - Configuration of the camera to use the lowest supported preview size and frame rate to reduce processing load.
6. **Performance Optimization**:
    - Techniques implemented to optimize the app's performance, especially regarding the calculation of average values and chart updates.
7. **Utilization of View Binding**:
    - The app uses **View Binding** to interact with XML views safely and efficiently.
    - View Binding reduces the risk of crashes caused by `NullPointerException` errors, improves code readability, and facilitates maintenance by automatically generating binding classes for each XML layout, eliminating the need to call `findViewById()`.

### Final Considerations

The use of View Binding in the project ensures that interactions with views are safe and free from runtime errors, while also improving code readability and maintainability. This practice, together with the use of other technologies and libraries such as `LiveData`, `ViewModel`, `Room`, and `ConstraintLayout`, contributes to creating a robust and performant app.
