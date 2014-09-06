package com.github.rahulsom.maas

class Loinc {

  static searchable = true

  String id
  String component
  String property
  String timeAspct
  String system
  String scaleTyp
  String methodTyp
  String class_
  String source
  Date dateLastChanged
  String chngType
  String comments
  String status
  String consumerName
  String molarMass
  Integer classtype
  String formula
  String species
  String exmplAnswers
  String acssym
  String baseName
  String naaccrId
  String codeTable
  String surveyQuestText
  String surveyQuestSrc
  String unitsrequired
  String submittedUnits
  String relatednames2
  String shortname
  String orderObs
  String cdiscCommonTests
  String hl7FieldSubfieldId
  String externalCopyrightNotice
  String exampleUnits
  String longCommonName
  String hl7V2Datatype
  String hl7V3Datatype
  String curatedRangeAndUnits
  String documentSection
  String exampleUcumUnits
  String exampleSiUcumUnits
  String statusReason
  String statusText
  String changeReasonPublic
  Integer commonTestRank
  Integer commonOrderRank
  Integer commonSiTestRank
  String hl7AttachmentStructure

  static mapping = {
    id generator: "assigned"
    version false
  }

  static constraints = {
    id maxSize: 10
    component nullable: true
    property nullable: true, maxSize: 30
    timeAspct nullable: true, maxSize: 15
    system nullable: true, maxSize: 100
    scaleTyp nullable: true, maxSize: 30
    methodTyp nullable: true, maxSize: 50
    class_ nullable: true, maxSize: 20
    source nullable: true, maxSize: 8
    dateLastChanged nullable: true
    chngType nullable: true, maxSize: 3
    comments nullable: true, maxSize: 65535
    status nullable: true, maxSize: 11
    consumerName nullable: true
    molarMass nullable: true, maxSize: 13
    classtype nullable: true
    formula nullable: true
    species nullable: true, maxSize: 20
    exmplAnswers nullable: true, maxSize: 65535
    acssym nullable: true, maxSize: 65535
    baseName nullable: true, maxSize: 50
    naaccrId nullable: true, maxSize: 20
    codeTable nullable: true, maxSize: 10
    surveyQuestText nullable: true, maxSize: 65535
    surveyQuestSrc nullable: true, maxSize: 50
    unitsrequired nullable: true, maxSize: 1
    submittedUnits nullable: true, maxSize: 30
    relatednames2 nullable: true, maxSize: 65535
    shortname nullable: true, maxSize: 40
    orderObs nullable: true, maxSize: 15
    cdiscCommonTests nullable: true, maxSize: 1
    hl7FieldSubfieldId nullable: true, maxSize: 50
    externalCopyrightNotice nullable: true, maxSize: 65535
    exampleUnits nullable: true
    longCommonName nullable: true
    hl7V2Datatype nullable: true
    hl7V3Datatype nullable: true
    curatedRangeAndUnits nullable: true, maxSize: 65535
    documentSection nullable: true
    exampleUcumUnits nullable: true
    exampleSiUcumUnits nullable: true
    statusReason nullable: true, maxSize: 9
    statusText nullable: true, maxSize: 65535
    changeReasonPublic nullable: true, maxSize: 65535
    commonTestRank nullable: true
    commonOrderRank nullable: true
    commonSiTestRank nullable: true
    hl7AttachmentStructure nullable: true, maxSize: 15
  }
}
