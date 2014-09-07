package com.github.rahulsom.maas

import groovy.transform.Canonical

/**
 * Created by rahulsomasunderam on 9/6/14.
 */

@Canonical
class ListResponse<E> {
  Long total
  List<E> list
}
