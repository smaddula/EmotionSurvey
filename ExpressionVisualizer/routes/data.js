/**
 * Created by siddardha on 8/28/2015.
 */

var express = require('express');
var router = express.Router();
var Parse = require('../src/ParseRetrieveData')

/* GET home page. */
router.get('/users', function(req, res, next) {
    Parse.GetUsers().then(
        function (results) {
           res.send(results);
        } ,
         function (error) {
             console.log(error);
         }
    );

    //res.send( Parse.GetUsers());
    //res.send( [{username:'sid'},{username:'kusu'}]);
    //return retn;
    //res.send( Parse.GetUsers());
    //res.send([{username:'sid'} ,{username:'kusuma'}]);
});


/* GET home page. */
router.get('/Surveys/:userObjectId', function(req, res, next) {
    Parse.GetSurveys(req.params.userObjectId).then(
        function (results) {
            res.send(results);
        } ,
        function (error) {
            console.log(error);
        }
    );
});

router.get('/SurveyData/:surveyId', function(req, res, next) {
    Parse.GetSurveyId(req.params.surveyId).then(
        function (results) {
            res.send(results);
        } ,
        function (error) {
            console.log(error);
        }
    );
});

module.exports = router;
