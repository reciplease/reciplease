Feature: House settings: member roles and invite management

  Background:
    Given a house "Test House" owned by "owner-1"

  Scenario: An owner promotes a read-only member to owner
    Given an invite code "PROMO1000000000000000000" for the house with role "READ_ONLY"
    And "member-1" accepts invite "PROMO1000000000000000000"
    When "owner-1" sets the role of "member-1" to "OWNER" in the house
    Then the response status is 200
    And "member-1" has role "OWNER" in the house

  Scenario: A read-only member cannot change anyone's role
    Given an invite code "PROMO2000000000000000000" for the house with role "READ_ONLY"
    And "member-2" accepts invite "PROMO2000000000000000000"
    When "member-2" sets the role of "member-2" to "OWNER" in the house
    Then the response status is 403

  Scenario: An owner generates an invite and a second user accepts it
    When "owner-1" generates an invite with role "READ_ONLY" in the house
    Then the response status is 201
    When "newcomer-3" accepts the generated invite
    Then the response status is 200
    And "newcomer-3" has role "READ_ONLY" in the house

  Scenario: An owner sees pending invites but not already-accepted ones
    Given an invite code "PENDING10000000000000000" for the house with role "READ_ONLY"
    And "member-3" accepts invite "PENDING10000000000000000"
    When "owner-1" generates an invite with role "OWNER" in the house
    And "owner-1" lists pending invites in the house
    Then the response status is 200
    And the pending invites list does not contain code "PENDING10000000000000000"

  Scenario: An owner deletes a pending invite, revoking it
    When "owner-1" generates an invite with role "READ_ONLY" in the house
    And "owner-1" deletes the generated invite in the house
    Then the response status is 204
    When "newcomer-4" accepts the generated invite
    Then the response status is 404
