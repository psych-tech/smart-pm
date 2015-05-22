'use strict';

angular.module('emolanceApp')
    .factory('Report', function ($resource) {
        return $resource('api/reports/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    if (data.timestamp != null) data.timestamp = new Date(data.timestamp);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    });
