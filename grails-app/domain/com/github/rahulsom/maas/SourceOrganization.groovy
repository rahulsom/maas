package com.github.rahulsom.maas

class SourceOrganization {

  String copyrightId
  String name
  String copyright
  String termsOfUse
  String url

  static mapping = {
    id name: "copyrightId", generator: "assigned"
    version false
  }

  static constraints = {
    name nullable: true
    copyright nullable: true, maxSize: 65535
    termsOfUse nullable: true, maxSize: 65535
    url nullable: true
  }
}
