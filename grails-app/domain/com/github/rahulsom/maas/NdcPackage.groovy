package com.github.rahulsom.maas

class NdcPackage {

  static searchable = [
      root: false,
  ]
  String ndcPackageCode
  String packageDescription

  static belongsTo = [
      product: NdcProduct
  ]

  static constraints = {
    packageDescription maxSize: 4000
  }
}
