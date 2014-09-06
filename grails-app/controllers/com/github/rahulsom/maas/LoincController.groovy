package com.github.rahulsom.maas

import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.*

@Transactional(readOnly = true)
@Secured('permitAll')
class LoincController {

  def elasticSearchService
  static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

  def index(Integer max) {
    params.max = Math.min(max ?: 10, 100)
    if (params.q) {
      def search = Loinc.search(params.q, params)
      respond search.searchResults, model: [loincInstanceCount: search.total]
    } else {
      respond Loinc.list(params), model: [loincInstanceCount: Loinc.count()]
    }
  }

  def show(Loinc loincInstance) {
    respond loincInstance
  }

  def create() {
    respond new Loinc(params)
  }

  @Transactional
  def save(Loinc loincInstance) {
    if (loincInstance == null) {
      notFound()
      return
    }

    if (loincInstance.hasErrors()) {
      respond loincInstance.errors, view: 'create'
      return
    }

    loincInstance.save flush: true

    request.withFormat {
      form multipartForm {
        flash.message = message(code: 'default.created.message', args: [message(code: 'loinc.label', default: 'Loinc'), loincInstance.id])
        redirect loincInstance
      }
      '*' { respond loincInstance, [status: CREATED] }
    }
  }

  def edit(Loinc loincInstance) {
    respond loincInstance
  }

  @Transactional
  def update(Loinc loincInstance) {
    if (loincInstance == null) {
      notFound()
      return
    }

    if (loincInstance.hasErrors()) {
      respond loincInstance.errors, view: 'edit'
      return
    }

    loincInstance.save flush: true

    request.withFormat {
      form multipartForm {
        flash.message = message(code: 'default.updated.message', args: [message(code: 'loinc.label', default: 'Loinc'), loincInstance.id])
        redirect loincInstance
      }
      '*' { respond loincInstance, [status: OK] }
    }
  }

  @Transactional
  def delete(Loinc loincInstance) {

    if (loincInstance == null) {
      notFound()
      return
    }

    loincInstance.delete flush: true

    request.withFormat {
      form multipartForm {
        flash.message = message(code: 'default.deleted.message', args: [message(code: 'loinc.label', default: 'Loinc'), loincInstance.id])
        redirect action: "index", method: "GET"
      }
      '*' { render status: NO_CONTENT }
    }
  }

  protected void notFound() {
    request.withFormat {
      form multipartForm {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'loinc.label', default: 'Loinc'), params.id])
        redirect action: "index", method: "GET"
      }
      '*' { render status: NOT_FOUND }
    }
  }
}
