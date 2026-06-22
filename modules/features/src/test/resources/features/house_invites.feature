Feature: House invites and role-based access

  Background:
    Given a house "Test House" owned by "owner-1"

  Scenario: Accepting a valid invite allowlists the user and grants the invited role
    Given an invite code "INVITE-1" for the house with role "READ_ONLY"
    When "newcomer-1" accepts invite "INVITE-1"
    Then the response status is 200
    And "newcomer-1" is on the allowlist
    And "newcomer-1" has role "READ_ONLY" in the house

  Scenario: An invite code can only be redeemed once
    Given an invite code "INVITE-2" for the house with role "READ_ONLY"
    And "newcomer-1" accepts invite "INVITE-2"
    When "newcomer-2" accepts invite "INVITE-2"
    Then the response status is 404

  Scenario: An owner can create inventory in their house
    When "owner-1" creates an inventory item named "Milk" in the house
    Then the response status is 201

  Scenario: A read-only member cannot create inventory
    Given an invite code "INVITE-3" for the house with role "READ_ONLY"
    And "member-1" accepts invite "INVITE-3"
    When "member-1" creates an inventory item named "Milk" in the house
    Then the response status is 403

  Scenario: A read-only member can list inventory in their own house
    Given an invite code "INVITE-4" for the house with role "READ_ONLY"
    And "member-2" accepts invite "INVITE-4"
    And "owner-1" creates an inventory item named "Eggs" in the house
    When "member-2" lists inventory in the house
    Then the response status is 200
    And the inventory list contains "Eggs"

  Scenario: A member of one house cannot see another house's inventory
    Given a house "Other House" owned by "owner-2"
    And "owner-2" creates an inventory item named "Secret Sauce" in house "Other House"
    When "owner-1" lists inventory in the house
    Then the response status is 200
    And the inventory list does not contain "Secret Sauce"
