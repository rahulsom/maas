package com.github.rahulsom.metrics

import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ADMIN'])
class StatusController {

    def index() { }
}
