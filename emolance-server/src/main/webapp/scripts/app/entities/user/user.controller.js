'use strict';

angular.module('emolanceApp')
    .controller('UserController', function ($scope, User) {
        $scope.users = [];
        $scope.loadAll = function() {
            User.query(function(result) {
               $scope.users = result;
            });
        };
        $scope.loadAll();

        $scope.showUpdate = function (id) {
            User.get({id: id}, function(result) {
                $scope.user = result;
                $('#saveUserModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.user.id != null) {
                User.update($scope.user,
                    function () {
                        $scope.refresh();
                    });
            } else {
                User.save($scope.user,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            User.get({id: id}, function(result) {
                $scope.user = result;
                $('#deleteUserConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            User.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteUserConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveUserModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.user = {subId: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
