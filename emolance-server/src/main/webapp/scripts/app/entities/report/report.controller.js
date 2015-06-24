'use strict';

angular.module('emolanceApp')
    .controller('ReportController', function ($scope, Report, User, ParseLinks) {
        $scope.reports = [];
        $scope.users = User.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            Report.query({page: $scope.page, per_page: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.reports = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.showUpdate = function (id) {
            Report.get({id: id}, function(result) {
                $scope.report = result;
                $('#saveReportModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.report.id != null) {
                Report.update($scope.report,
                    function () {
                        $scope.refresh();
                    });
            } else {
                Report.save($scope.report,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            Report.get({id: id}, function(result) {
                $scope.report = result;
                $('#deleteReportConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Report.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteReportConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveReportModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.report = {type: null, value: null, timestamp: null, qrcode: null, status: null, name: null, link: null, age: null, position: null, email: null, result: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
