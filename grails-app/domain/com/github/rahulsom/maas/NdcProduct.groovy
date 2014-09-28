package com.github.rahulsom.maas

class NdcProduct {

  static searchable = {
    packages component: true
  }

  String id
  String productNdc
  String productTypeName
  String proprietaryName
  String proprietaryNameSuffix
  String nonProprietaryName
  String dosageFormName
  String routeName
  Date startMarketingDate
  Date endMarketingDate
  String marketingCategoryName
  String applicationNumber
  String labelerName
  String substanceName
  String activeNumeratorStrength
  String activeIngredUnit
  String pharmClasses
  String deaSchedule

  static constraints = {
    deaSchedule nullable: true
    endMarketingDate nullable: true
    proprietaryNameSuffix nullable: true
    activeIngredUnit nullable: true, maxSize: 4000
    activeNumeratorStrength nullable: true, maxSize: 4000
    pharmClasses nullable: true
    substanceName nullable: true, maxSize: 4000
    routeName nullable: true
    pharmClasses maxSize: 4000
    applicationNumber nullable: true
    nonProprietaryName maxSize: 4000, nullable: true
    proprietaryName nullable: true
    startMarketingDate nullable: true
  }

  static mapping = {
    id generator: "assigned"
    version false
  }

  static hasMany = [
      packages: NdcPackage
  ]
}
