/**
 * Created by siddardha on 8/28/2015.
 */

var Parse = require('parse').Parse;
var requestModule = require("request");


module.exports =
{
    initialize:function () {
        Parse.initialize("DYlc2hnyF3zlDZNcxhDE2zgSk87eQOdRCOJhgVSQ", "KdMQLJUBZ4vCTS78XTCVwCPpnlRvjnDWk8mef79l");
    },

    GetUsers:function (){
        var surveyUsers = Parse.Object.extend("User");
        var query = new Parse.Query(surveyUsers);
        var rtn = [];

        return query.find().then(function(res){
            res.forEach(function(value , index , ar)  {
                console.log(value.get("username"));
                rtn.push({ username: value.get("username") , objectId: value.id});
            });
            return rtn;
        } ,
        function(fail){
            console.log("failed");
        });
    },

    GetSurveys:function (userObjectID){
        var User = Parse.Object.extend("User");
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
            });
    },


    GetSurveyId:function (surveyId){
        var SurveyData = Parse.Object.extend("SurveyData");
        var query = new Parse.Query(SurveyData);
        return query.get(
            surveyId).then(function(res) {
                return requestModule({
                        url: res.get("JsonEmotionData")._url,
                        json: true,
                    }, function (error, response, body) {
                        if (!error && response.statusCode === 200) {
                            console.log(body) // Print the json response
                            return body;
                        }
                    }
                )
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