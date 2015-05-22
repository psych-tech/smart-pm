'use strict';

angular.module('emolanceApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
