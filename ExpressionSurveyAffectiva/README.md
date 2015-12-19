Two main Folders

1) ParseUI - This is the login screen , this component is open source (google for parse login ui). Made changes to ParseSignupFragment to store Sex and Age of the user. You need to add these 2 columns to the Parse Users table

2) App - This folder has all the implementations of the application including all activities and layouts


Architecture of the Application

Application has one major Activity called MainActivity which has a layout of activity_main . This activity does the following tasks.

1) Affectiva Logic 
2) UI related logics ie navigating to next image 

Explaining each of the task below
1) Affectiva Logic - Main activity implements FaceListener, ImageListener which are affectiva's interfaces. Explaining the affectiva's functions and also OnCreate 
a) onFaceDetectionStopped - When face is not detected then this event is triggered where we are hiding the image , radio buttons and 
buttons so that the surfaceview which displays user face takes up the whole screen.
b) onFaceDetectionStarted - If face got detected then this event is triggered . In this event we restore the visibility of the components that are required for the survey to be continued and shrink the surfaceView
c) onCreate - This is the location where the activity is initialized . this is the place where all affectiva's compononets and background threads are initialized. We also set frame rate for affectiva here.
d) onImageResults - This is called for each user's image processed by affectiva . This image is sent to background thread to save the file in S3. This also has details of user's emotion derived from this image.

2) UI related Logics - Several functions here which guide logic like changing images in intensity when valence is changed , 



Res Folder

Layouts - The app has 4 layouts 

1) activity_cache_images.xml - This is the layout where progress bar is shown to indicate the progress of the survey images being downloaded and cached. This screen will automatically direct you to activity_main.xml once all images are downloaded.

2) activity_confirmation_start.xml - This is the simple confirmation screen that pops up when user clicks start survey .This screen will lead you to activity_cache_images.xml

3) activity_main.xml - This is the only layout that is used during the survey. This has survey image , one radio group representing valence , one radio group representing intensity , one surface view for camera , Buttons next and save , ProgressBar . All these views are placed inside various layouts which are resized or hidden based on 1) Face being detected 2) Show different controls on last image like save and progress bar. Completion of this activity ie end of survey will take you to login screen ie ParseUI

4) activity_user_pick.xml - This is the layout which gives two options to users which are logoff or start survey. Start survey will take you to activity_confirmation_start while logoff will take you to ParseUI

Drawable - Smileys

Each radio button can have 2 states ie selected and non-selected image used in the radio button is dynamically changed based on the state of the radio button. This is achieved by using xml selectors , these are present in drawable folder along with the png images that represent all possible smileys . This model is inspired from http://stackoverflow.com/questions/19163628/adding-custom-radio-buttons-in-android

