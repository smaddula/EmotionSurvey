# EmotionSurvey

### EmotionSurveyAffectiva:

This is an Android application that captures user emotions while he is taking a survey and stores the data in parse database.

SDK's Used

1. Parse SDK -Get keys from parse website and add a strings.xml file in res/values that has keys parse_app_id , parse_client_key. 

2. Affedex SDK - Get licence file from Affedex and place it in assets/Affedex folder . Currently there is a licence file in this repository but its expired

3. AWS Android SDK - Using AWS Conginto to provide permissions to S3 bucket use for uploading images - Details can be found in utils.java file . Keys can be configured through AWS console . Android SDK can be obtained from https://aws.amazon.com/mobile/sdk/

### EmotionVisualizer:

This is a nodejs project which shows charts to help analyze data collected through the survey . D3 was used for visualizations .

This website is deployed at http://emotion-maddula.rhcloud.com


