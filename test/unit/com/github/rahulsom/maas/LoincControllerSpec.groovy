package com.github.rahulsom.maas

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(LoincController)
@Mock(Loinc)
class LoincControllerSpec extends Specification {

  def populateValidParams(params) {
    assert params != null
    // TODO: Populate valid properties like...
    //params["name"] = 'someValidName'
  }

  void "Test the index action returns the correct model"() {

    when: "The index action is executed"
    controller.index()

    then: "The model is correct"
    !model.loincInstanceList
    model.loincInstanceCount == 0
  }

  void "Test the create action returns the correct model"() {
    when: "The create action is executed"
    controller.create()

    then: "The model is correctly created"
    model.loincInstance != null
  }

  void "Test the save action correctly persists an instance"() {

    when: "The save action is executed with an invalid instance"
    request.contentType = FORM_CONTENT_TYPE
    request.method = 'POST'
    def loinc = new Loinc()
    loinc.validate()
    controller.save(loinc)

    then: "The create view is rendered again with the correct model"
    model.loincInstance != null
    view == 'create'

    when: "The save action is executed with a valid instance"
    response.reset()
    populateValidParams(params)
    loinc = new Loinc(params)

    controller.save(loinc)

    then: "A redirect is issued to the show action"
    response.redirectedUrl == '/loinc/show/1'
    controller.flash.message != null
    Loinc.count() == 1
  }

  void "Test that the show action returns the correct model"() {
    when: "The show action is executed with a null domain"
    controller.show(null)

    then: "A 404 error is returned"
    response.status == 404

    when: "A domain instance is passed to the show action"
    populateValidParams(params)
    def loinc = new Loinc(params)
    controller.show(loinc)

    then: "A model is populated containing the domain instance"
    model.loincInstance == loinc
  }

  void "Test that the edit action returns the correct model"() {
    when: "The edit action is executed with a null domain"
    controller.edit(null)

    then: "A 404 error is returned"
    response.status == 404

    when: "A domain instance is passed to the edit action"
    populateValidParams(params)
    def loinc = new Loinc(params)
    controller.edit(loinc)

    then: "A model is populated containing the domain instance"
    model.loincInstance == loinc
  }

  void "Test the update action performs an update on a valid domain instance"() {
    when: "Update is called for a domain instance that doesn't exist"
    request.contentType = FORM_CONTENT_TYPE
    request.method = 'PUT'
    controller.update(null)

    then: "A 404 error is returned"
    response.redirectedUrl == '/loinc/index'
    flash.message != null


    when: "An invalid domain instance is passed to the update action"
    response.reset()
    def loinc = new Loinc()
    loinc.validate()
    controller.update(loinc)

    then: "The edit view is rendered again with the invalid instance"
    view == 'edit'
    model.loincInstance == loinc

    when: "A valid domain instance is passed to the update action"
    response.reset()
    populateValidParams(params)
    loinc = new Loinc(params).save(flush: true)
    controller.update(loinc)

    then: "A redirect is issues to the show action"
    response.redirectedUrl == "/loinc/show/$loinc.id"
    flash.message != null
  }

  void "Test that the delete action deletes an instance if it exists"() {
    when: "The delete action is called for a null instance"
    request.contentType = FORM_CONTENT_TYPE
    request.method = 'DELETE'
    controller.delete(null)

    then: "A 404 is returned"
    response.redirectedUrl == '/loinc/index'
    flash.message != null

    when: "A domain instance is created"
    response.reset()
    populateValidParams(params)
    def loinc = new Loinc(params).save(flush: true)

    then: "It exists"
    Loinc.count() == 1

    when: "The domain instance is passed to the delete action"
    controller.delete(loinc)

    then: "The instance is deleted"
    Loinc.count() == 0
    response.redirectedUrl == '/loinc/index'
    flash.message != null
  }
}
