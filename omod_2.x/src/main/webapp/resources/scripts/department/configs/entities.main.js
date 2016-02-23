/* initialize and bootstrap application */
requirejs(['department/configs/entities.module'], function() {
    requirejs(['reusable-components/lib/domReady'], function(domReady) {
        domReady(function() {
            angular.bootstrap(domReady, ['entitiesApp']);
        });
    });
});