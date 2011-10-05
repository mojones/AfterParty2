package afterparty

import org.apache.commons.lang.builder.HashCodeBuilder

class AfterpartyUserAfterPartyRole implements Serializable {

    AfterpartyUser afterpartyUser
    AfterPartyRole afterPartyRole

    boolean equals(other) {
        if (!(other instanceof AfterpartyUserAfterPartyRole)) {
            return false
        }

        other.afterpartyUser?.id == afterpartyUser?.id &&
                other.afterPartyRole?.id == afterPartyRole?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (afterpartyUser) builder.append(afterpartyUser.id)
        if (afterPartyRole) builder.append(afterPartyRole.id)
        builder.toHashCode()
    }

    static AfterpartyUserAfterPartyRole get(long afterpartyUserId, long afterPartyRoleId) {
        find 'from AfterpartyUserAfterPartyRole where afterpartyUser.id=:afterpartyUserId and afterPartyRole.id=:afterPartyRoleId',
                [afterpartyUserId: afterpartyUserId, afterPartyRoleId: afterPartyRoleId]
    }

    static AfterpartyUserAfterPartyRole create(AfterpartyUser afterpartyUser, AfterPartyRole afterPartyRole, boolean flush = false) {
        new AfterpartyUserAfterPartyRole(afterpartyUser: afterpartyUser, afterPartyRole: afterPartyRole).save(flush: flush, insert: true)
    }

    static boolean remove(AfterpartyUser afterpartyUser, AfterPartyRole afterPartyRole, boolean flush = false) {
        AfterpartyUserAfterPartyRole instance = AfterpartyUserAfterPartyRole.findByAfterpartyUserAndAfterPartyRole(afterpartyUser, afterPartyRole)
        instance ? instance.delete(flush: flush) : false
    }

    static void removeAll(AfterpartyUser afterpartyUser) {
        executeUpdate 'DELETE FROM AfterpartyUserAfterPartyRole WHERE afterpartyUser=:afterpartyUser', [afterpartyUser: afterpartyUser]
    }

    static void removeAll(AfterPartyRole afterPartyRole) {
        executeUpdate 'DELETE FROM AfterpartyUserAfterPartyRole WHERE afterPartyRole=:afterPartyRole', [afterPartyRole: afterPartyRole]
    }

    static mapping = {
        id composite: ['afterPartyRole', 'afterpartyUser']
        version false
    }
}
