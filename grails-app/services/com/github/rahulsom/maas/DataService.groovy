package com.github.rahulsom.maas

import grails.compiler.GrailsCompileStatic
import org.apache.commons.lang.StringUtils
import org.grails.plugins.elasticsearch.ElasticSearchService
import org.h2.tools.Csv
import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.hibernate.Transaction

import java.text.SimpleDateFormat

@GrailsCompileStatic
class DataService {

  def sessionFactory
  def elasticSearchService

  void storeLoinc(String loincFile) {
    StatelessSession session = ((SessionFactory)sessionFactory).openStatelessSession()
    Transaction tx = session.beginTransaction()

    Loinc.executeUpdate('DELETE from Loinc')

    def resultSet = new Csv().read(new FileReader(loincFile), null)
    def resultSetMetaData = resultSet.metaData

    Map<String,String> fieldNameMap = (1..(resultSetMetaData.columnCount)).collect().collectEntries { int i ->
      def colName = resultSetMetaData.getColumnName(i)
      def fieldName = StringUtils.uncapitalize(colName.split('_').collect { String it ->
        StringUtils.capitalize(it.toLowerCase())
      }.join(''))
      if (fieldName == 'class') {
        fieldName += '_'
      }
      [colName, fieldName]
    }

    int batchSize = 0
    long lastCheck = System.nanoTime()
    while (resultSet.next()) {
      Loinc loinc = new Loinc()
      fieldNameMap.each { String k, String v ->
        if (v == 'loincNum') {
          loinc.id = resultSet.getString(k)
        } else if (loinc.metaClass.properties.find { it.name == v }.type.isAssignableFrom(String)) {
          loinc[v] = resultSet.getString(k)
        } else if (loinc.metaClass.properties.find { it.name == v }.type.isAssignableFrom(Integer)) {
          loinc[v] = resultSet.getInt(k)
        } else if (loinc.metaClass.properties.find { it.name == v }.type.isAssignableFrom(Date)) {
          def strVal = resultSet.getString(k)
          if (strVal) {
            loinc[v] = new SimpleDateFormat('yyyyMMdd').parse(strVal)
          }
        } else {
          println "Unhandled field type: ${loinc.metaClass.properties[v].class}"
        }
      }
      session.insert(loinc)
      if (++batchSize %200 == 0) {
        long newCheck = System.nanoTime()
        System.out.print "\r ${batchSize} loincs down in ${(newCheck - lastCheck)/1000000.0} ms"
      }

    }

    tx.commit()
    session.close()

    ((ElasticSearchService)elasticSearchService).unindex(Loinc)
    ((ElasticSearchService)elasticSearchService).index(Loinc)
  }

  void storeNdc(String productFile, String packageFile, Map<String,String> errata, List<String> badIds) {
    StatelessSession session = ((SessionFactory)sessionFactory).openStatelessSession()
    Transaction tx = session.beginTransaction()

    NdcProduct.executeUpdate('DELETE from NdcPackage')
    NdcProduct.executeUpdate('DELETE from NdcProduct')

    def prodResultSet = new Csv(fieldSeparatorRead: '\t' as char).read(new FileReader(productFile), null)
    def packResultSet = new Csv(fieldSeparatorRead: '\t' as char).read(new FileReader(packageFile), null)
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
        System.out.print "\r ${batchSize} products down in ${(newCheck - lastCheck)/1000000.0} ms"
      }

    }
    tx.commit()
    tx = session.beginTransaction()
    batchSize = 0
    lastCheck = System.nanoTime()
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
        System.out.print "\r ${batchSize} packages down in ${(newCheck - lastCheck)/1000000.0} ms"
      }
    }

    tx.commit()
    session.close()

    ((ElasticSearchService)elasticSearchService).unindex(NdcProduct)
    ((ElasticSearchService)elasticSearchService).index(NdcProduct)
  }
}
