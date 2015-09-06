var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/survey/:surveyId', function(req, res, next) {

  res.render('ShowSurvey', { surveyId: req.params.surveyId });
});

module.exports = router;
