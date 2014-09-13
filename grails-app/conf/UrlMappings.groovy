class UrlMappings {

  static mappings = {
    "/loinc" (resources: 'loinc')

    "/$controller/$action?/$id?(.$format)?" {
      constraints {
        // apply constraints here
      }
    }

    "/"(view: "/index")
    "500"(view: '/error')
  }
}
