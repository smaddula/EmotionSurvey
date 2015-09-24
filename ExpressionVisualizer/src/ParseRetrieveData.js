/**
 * Created by siddardha on 8/28/2015.
 */

var Parse = require('parse').Parse;
var requestModule = require("request");
var Promise = require('promise');

module.exports =
{
    initialize: function () {
        Parse.initialize("DYlc2hnyF3zlDZNcxhDE2zgSk87eQOdRCOJhgVSQ", "KdMQLJUBZ4vCTS78XTCVwCPpnlRvjnDWk8mef79l");
    },

    GetUsers: function () {
        var surveyUsers = Parse.Object.extend("User");
        var query = new Parse.Query(surveyUsers);
        var rtn = [];

        return query.find().then(function (res) {
                res.forEach(function (value, index, ar) {
                    console.log(value.get("username"));
                    rtn.push({username: value.get("username"), objectId: value.id});
                });
                return rtn;
            },
            function (fail) {
                console.log("failed");
            });
    },

    GetSurveys: function (userObjectID) {
        var User = Parse.Object.extend("User");
        var selecteduser = new User();
        selecteduser.id = userObjectID;


        var surveyedData = Parse.Object.extend("SurveyData");
        var query = new Parse.Query(surveyedData);
        query.equalTo("UserID", selecteduser);
        query.ascending("createdAt");
        var rtn = [];

        return query.find().then(function (res) {
                res.forEach(function (value, index, ar) {
                    console.log(value.createdAt);
                    rtn.push({createdat: value.createdAt , objectId: value.id});
                });
                return rtn;
            },
            function (fail) {
                console.log("failed");
            });
    },


    GetSurveyId: function (surveyId) {
        var SurveyData = Parse.Object.extend("SurveyData");
        var query = new Parse.Query(SurveyData);
        var rtn;
        return query.get(
            surveyId).then(
            function (res) {
                return new Promise(function (resolve, reject) {
                    requestModule({
                            url: res.get("JsonEmotionData")._url,
                            json: true,
                        }, function (error, response, body) {
                            if (!error && response.statusCode === 200) {
                                console.log(body) // Print the json response
                                rtn = body;
                                resolve(rtn);
                            } else {
                                if (error) reject(error);
                            }
                        }
                    )
                })
            },
            function (error) {
                console.log(error);
            }
        );


        /*
         var selecteduser = new User();
         selecteduser .id = userObjectID;


         var surveyedData = Parse.Object.extend("SurveyData");
         var query = new Parse.Query(surveyedData);
         query.equalTo("UserID",selecteduser );
         var rtn = [];

         return query.find().then(function(res){
         res.forEach(function(value , index , ar)  {
         console.log(value.createdAt);
         rtn.push({ createdat: value.createdAt , objectId: value.id});
         });
         return rtn;
         } ,
         function(fail){
         console.log("failed");
         });*/
    }
}