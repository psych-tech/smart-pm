'use strict';

angular.module('emolanceApp')
    .factory('Device', function ($resource) {
        return $resource('api/devices/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    if (data.regTime != null) data.regTime = new Date(data.regTime);
                    if (data.lastUpdate != null) data.lastUpdate = new Date(data.lastUpdate);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    });
