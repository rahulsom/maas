import com.github.rahulsom.maas.Loinc
import com.github.rahulsom.maas.auth.Role
import com.github.rahulsom.maas.auth.User
import com.github.rahulsom.maas.auth.UserRole
import org.apache.commons.lang.StringUtils
import org.h2.tools.Csv

import java.text.SimpleDateFormat

class BootStrap {

  def elasticSearchService

  def init = { servletContext ->

    User.withNewTransaction {
      def adminRole = new Role(authority: 'ROLE_ADMIN').save(flush: true)
      def userRole = new Role(authority: 'ROLE_USER').save(flush: true)

      def admin = new User(username: 'admin', password: 'admin').save(flush: true)
      def user = new User(username: 'user', password: 'user').save(flush: true)

      UserRole.create admin, adminRole, true
      UserRole.create user, userRole, true
    }

  }
  def destroy = {
  }
}
