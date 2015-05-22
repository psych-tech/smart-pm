'use strict';

angular.module('emolanceApp')
    .controller('DeviceController', function ($scope, Device, User, ParseLinks) {
        $scope.devices = [];
        $scope.users = User.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            Device.query({page: $scope.page, per_page: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.devices = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.showUpdate = function (id) {
            Device.get({id: id}, function(result) {
                $scope.device = result;
                $('#saveDeviceModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.device.id != null) {
                Device.update($scope.device,
                    function () {
                        $scope.refresh();
                    });
            } else {
                Device.save($scope.device,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            Device.get({id: id}, function(result) {
                $scope.device = result;
                $('#deleteDeviceConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Device.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteDeviceConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveDeviceModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.device = {sn: null, regTime: null, lastUpdate: null, status: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
