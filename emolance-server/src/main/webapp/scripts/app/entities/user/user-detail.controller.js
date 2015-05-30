'use strict';

angular.module('emolanceApp')
    .controller('UserDetailController', function ($scope, $stateParams, User) {
        $scope.user = {};
        $scope.load = function (id) {
            User.get({id: id}, function(result) {
              $scope.user = result;
            });
        };
        $scope.load($stateParams.id);
    });
