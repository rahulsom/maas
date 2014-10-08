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
import grails.transaction.Transactional
import org.grails.plugins.metrics.groovy.Timed
import org.h2.tools.Csv
import org.hibernate.StatelessSession
import org.hibernate.Transaction

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional(readOnly = true)
@Secured('ROLE_USER')
@Api(value = "NDC Code", description = "NDC Codes",
    produces = 'application/json,application/hal+json,application/xml,text/html',
    consumes = 'application/json,application/xml,application/x-www-form-urlencoded'
)

class NdcProductController {

  def sessionFactory
  def elasticSearchService
  static allowedMethods = [save: "POST", ]

  @SwaggyList
  @Timed(name='ndcsearch')
  def index() {
    params.max = Math.min(params.max ?: 10, 100)
    if (params.q) {
      def search = NdcProduct.search(params.q, params)
      respond search.searchResults , model: [ndcProductInstanceCount: search.total]
    } else {
      respond NdcProduct.list(params), model: [ndcProductInstanceCount: NdcProduct.count()]
    }
  }

  @SwaggyShow
  @Timed(name='ndcshow')
  def show() {
    respond NdcProduct.get(params.id)
  }

  @Secured('ROLE_ADMIN')
  @ApiOperation(value = "Save NDC Codes", response = Void)
  @ApiResponses([
      @ApiResponse(code = 422, message = 'Bad Entity Received'),
  ])
  @ApiImplicitParams([
      @ApiImplicitParam(name = 'package', paramType = 'form', required = true, dataType = 'string',
          value="CSV File from downloaded zip. E.g. '/opt/ndc/package.txt'"),
      @ApiImplicitParam(name = 'product', paramType = 'form', required = true, dataType = 'string',
          value="CSV File from downloaded zip. E.g. '/opt/ndc/product.txt'"),
  ])
  def save() {
    StatelessSession session = sessionFactory.openStatelessSession()
    Transaction tx = session.beginTransaction()

    NdcProduct.executeUpdate('DELETE from NdcPackage')
    NdcProduct.executeUpdate('DELETE from NdcProduct')

    def prodResultSet = new Csv(fieldSeparatorRead: '\t' as char).read(new FileReader(params.product), null)
    def packResultSet = new Csv(fieldSeparatorRead: '\t' as char).read(new FileReader(params.package), null)
    def prodFields = [
        "PRODUCTID": "id",
        "PRODUCTNDC": "productNdc",
        "PRODUCTTYPENAME": "productTypeName",
        "PROPRIETARYNAME": "proprietaryName",
        "PROPRIETARYNAMESUFFIX": "proprietaryNameSuffix",
        "NONPROPRIETARYNAME": "nonProprietaryName",
        "DOSAGEFORMNAME": "dosageFormName",
        "ROUTENAME": "routeName",
        "STARTMARKETINGDATE": "startMarketingDate",
        "ENDMARKETINGDATE": "endMarketingDate",
        "MARKETINGCATEGORYNAME": "marketingCategoryName",
        "APPLICATIONNUMBER": "applicationNumber",
        "LABELERNAME": "labelerName",
        "SUBSTANCENAME": "substanceName",
        "ACTIVE_NUMERATOR_STRENGTH": "activeNumeratorStrength",
        "ACTIVE_INGRED_UNIT": "activeIngredUnit",
        "PHARM_CLASSES": "pharmClasses",
        "DEASCHEDULE": "deaSchedule"
    ]
    NdcProduct ndcProduct = null
    int batchSize = 0
    long lastCheck = System.nanoTime()
    while (prodResultSet.next()) {
      ndcProduct = new NdcProduct()
      prodFields.each { String k, String v ->
        if (ndcProduct.metaClass.properties.find { it.name == v }.type.isAssignableFrom(String)) {
          ndcProduct[v] = prodResultSet.getString(k)
        } else if (ndcProduct.metaClass.properties.find { it.name == v }.type.isAssignableFrom(Integer)) {
          ndcProduct[v] = prodResultSet.getInt(k)
        } else if (ndcProduct.metaClass.properties.find { it.name == v }.type.isAssignableFrom(Date)) {
          def strVal = prodResultSet.getString(k)
          if (strVal) {
            ndcProduct[v] = new SimpleDateFormat('yyyyMMdd').parse(strVal)
          }
        } else {
          println "Unhandled field type: ${ndcProduct.metaClass.properties[v].class}"
        }
      }
      session.insert(ndcProduct)
      if (++batchSize %200 == 0) {
        long newCheck = System.nanoTime()
        println "${batchSize} products down in ${(newCheck - lastCheck)/1000000.0} ms"
        lastCheck = newCheck
      }

    }
    tx.commit()
    tx = session.beginTransaction()
    batchSize = 0
    while (packResultSet.next()) {
      String productId = packResultSet.getString('PRODUCTID')
      if (errata[productId]) {
        productId = errata[productId]
      }
      if (!badIds.contains(productId)) {
        def ndcPackage = new NdcPackage(
            ndcPackageCode: packResultSet.getString('NDCPACKAGECODE'),
            packageDescription: packResultSet.getString('PACKAGEDESCRIPTION'),
            product: NdcProduct.load(productId)
        )

        session.insert(ndcPackage)
      }
      if (++batchSize %200 == 0) {
        long newCheck = System.nanoTime()
        println "${batchSize} packages down in ${(newCheck - lastCheck)/1000000.0} ms"
        lastCheck = newCheck
      }
    }

    tx.commit()
    session.close()

    elasticSearchService.unindex(NdcProduct)
    elasticSearchService.index(NdcProduct)

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
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'ndcproduct.label', default: 'Ndc Product'), params.id])
        redirect action: "index", method: "GET"
      }
      '*' { render status: NOT_FOUND }
    }
  }

  static errata = [
      "0574-0129_1b2614a8-e016-467b-a25c-c9752e221098": "0574-0129_5e83529c-ac3d-4fc5-9361-7ad4a8776a39",
      "0597-0165_d1958afe-a1fc-7ea7-6eae-6a5f2413e44f": "0597-0165_9fbe496c-1aca-8846-5407-56018981c0b4",
      "0597-0170_d1958afe-a1fc-7ea7-6eae-6a5f2413e44f": "0597-0170_9fbe496c-1aca-8846-5407-56018981c0b4",
      "11410-914_2e24a563-b520-42ee-9d67-3768f4d91b49": "11410-914_e0d16282-9e70-401f-bd92-f73cd49f92ce",
      "11673-306_bb344822-81a0-47c8-875d-a98011f3aab9": "11673-306_57f82c03-6551-4c87-b1c3-3d27b1ea3475",
      "36800-205_4f439074-e7ce-4494-b33e-e9eaeb0a2698": "36800-205_6a64800b-5f32-462b-a457-a8b0fa56574d",
      "36800-306_afd92ccc-19b8-4aec-8e5b-ae1e6b890b32": "36800-306_03be459a-6a37-4812-8b5c-c6ff4fc05d9e",
      "41163-335_5e1cd969-7130-4ab1-a475-1dad6af1d966": "41163-335_d47526a7-e532-4bba-8516-d55161c13405",
      "41163-459_2ef27848-06a3-4d2f-beee-ca021e21d0d5": "41163-459_cff376ea-d34a-4696-aa9e-31d9fac5402b",
      "41520-578_0508fd83-4756-4715-aa96-892265275723": "41520-578_9462f1b9-7431-4cd1-8662-8e5a2691caae",
      "45802-868_ec0bad03-9e55-4c7f-a524-955f20b81078": "45802-868_45594dc9-319c-45a3-b657-bc84e973402a",
      "50580-324_91a08095-e829-4c13-9441-a149c2cddcca": "50580-324_5a3fb924-8254-4898-9a26-3eca6d897113",
      "52533-026_2f709e92-b47e-4140-8ca2-4a4e856a99a9": "52533-026_37658a77-4772-45e2-8566-ed7e246dd446",
      "55154-6755_adfd1230-4467-4783-86e6-2e745bc31b32": "55154-6755_9eca1b41-6747-48ae-942b-c102dc88173d",
      "59779-306_4bde319a-1b92-4fad-8ddd-a10c350c716b": "59779-306_3de458a1-2163-44cd-b0b6-9ff6ae023cf3",
      "76420-772_f4a07aaa-a7f6-49e1-b100-40725ffbd365": "76420-772_d9d0f05a-7c19-4ac2-bf44-a3d23fd8616b",
      "76420-782_2466e8b9-7011-4028-9ba3-8cc972290aa2": "76420-782_a6c5da87-a7b4-4685-bedc-45aa6855d3ba",
  ]

  static badIds = [
      "0597-0186_d1958afe-a1fc-7ea7-6eae-6a5f2413e44f",
      "48951-3100_bbc3bdf9-3b4a-4b64-bce4-09fe7a1995c9",
      "49967-007_f07b2ab5-096f-453e-89df-fdcd26f81054",
      "49967-045_e645ace1-d77e-4ca3-8b81-31579e4ef959",
      "49967-128_7df19ac5-9984-4d3d-a759-08e11e4ef800",
      "49967-427_941858bb-8672-421f-8fd8-d5225e9480c4",
      "49967-453_e5425723-8a7e-49d8-af21-2b0d7921b69d",
      "49967-524_dbcde648-fa77-4110-ba60-fc4c36f13f5d",
      "49967-969_a633efc4-c6c4-46df-8587-fb55049eb3c9",
      "61543-2285_76ab6950-f507-43f9-bcc4-7b314a349a7f",
      "76420-750_9b5da9eb-b6f6-41bd-a6fe-392cc229d16a",

  ]
}
