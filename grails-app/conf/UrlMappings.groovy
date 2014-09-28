class UrlMappings {

  static mappings = {
    "/loinc" (resources: 'loinc')
    "/ndc" (resources: 'ndcProduct')

    "/$controller/$action?/$id?(.$format)?" {
      constraints {
        // apply constraints here
      }
    }

    "/"(view: "/index")
    "500"(view: '/error')
  }
}
