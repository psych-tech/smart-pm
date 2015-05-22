'use strict';

angular.module('emolanceApp')
    .controller('ReportDetailController', function ($scope, $stateParams, Report, User) {
        $scope.report = {};
        $scope.load = function (id) {
            Report.get({id: id}, function(result) {
              $scope.report = result;
            });
        };
        $scope.load($stateParams.id);
    });
