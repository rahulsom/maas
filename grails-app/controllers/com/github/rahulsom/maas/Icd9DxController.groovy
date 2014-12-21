package com.github.rahulsom.maas

import com.github.rahulsom.swaggydoc.SwaggyList
import com.github.rahulsom.swaggydoc.SwaggyShow
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiImplicitParam
import com.wordnik.swagger.annotations.ApiImplicitParams
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponse
import com.wordnik.swagger.annotations.ApiResponses
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import org.grails.plugins.metrics.groovy.Timed

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
@Secured('ROLE_USER')
@Api(value = "ICD 9 Diagnosis", description = "ICD 9 Diagnoses",
    produces = 'application/json,application/hal+json,application/xml,text/html',
    consumes = 'application/json,application/xml,application/x-www-form-urlencoded'
)
class Icd9DxController {

  def dataService
  static allowedMethods = [save: "POST"]

  @SwaggyList
  @Timed(name='icd9dxsearch')
  def index() {
    params.max = Math.min(params.int('max') ?: 10, 100)
    params.offset =  params.int('offset') ?: 0
    if (params.q) {
      params.from = params.offset
      params.size = params.max
      def search = Icd9Dx.search(params.q, params)
      respond search.searchResults, model: [icd9DxInstanceCount: search.total]
    } else {
      respond Icd9Dx.list(params), model: [icd9DxInstanceCount: Icd9Dx.count()]
    }
  }

  @SwaggyShow
  @Timed(name='icd9dxshow')
  def show(Icd9Dx icd9DxInstance) {
    respond icd9DxInstance
  }

  @Secured('ROLE_ADMIN')
  @ApiOperation(value = "Save ICD9 Diagnosis Codes", response = Void)
  @ApiResponses([
      @ApiResponse(code = 422, message = 'Bad Entity Received'),
  ])
  @ApiImplicitParams([
      @ApiImplicitParam(name = 'shortFile', paramType = 'form', required = true, dataType = 'string',
          value="Flat File from downloaded zip. E.g. '/opt/icd9/CMS32_DESC_SHORT_DX.txt'"),
      @ApiImplicitParam(name = 'longFile', paramType = 'form', required = true, dataType = 'string',
          value="Flat File from downloaded zip. E.g. '/opt/icd9/CMS32_DESC_LONG_DX.txt'"),
  ])
  def save() {
    dataService.storeIcd9Dx(params.longFile, params.shortFile)

    withFormat {
      html {
        render("Complete!")
      }
      json {
        def retval = [status: 'Complete']
        render retval as JSON
      }
      xml {
        def retval = [status: 'Complete']
        render retval as XML
      }
    }
  }


  protected void notFound() {
    request.withFormat {
      form multipartForm {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'icd9Dx.label', default: 'Icd9Dx'), params.id])
        redirect action: "index", method: "GET"
      }
      '*' { render status: NOT_FOUND }
    }
  }
}
