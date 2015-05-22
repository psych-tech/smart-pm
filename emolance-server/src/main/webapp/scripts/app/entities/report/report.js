'use strict';

angular.module('emolanceApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('report', {
                parent: 'entity',
                url: '/report',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'emolanceApp.report.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/report/reports.html',
                        controller: 'ReportController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('report');
                        return $translate.refresh();
                    }]
                }
            })
            .state('reportDetail', {
                parent: 'entity',
                url: '/report/:id',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'emolanceApp.report.detail.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/report/report-detail.html',
                        controller: 'ReportDetailController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('report');
                        return $translate.refresh();
                    }]
                }
            });
    });
