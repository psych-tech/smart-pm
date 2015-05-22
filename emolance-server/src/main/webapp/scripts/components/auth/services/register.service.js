'use strict';

angular.module('emolanceApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


