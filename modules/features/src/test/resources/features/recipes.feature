Feature: Recipe upvoting and upvote-ordered listing

  Background:
    Given a house "Test House" owned by "owner-1"

  Scenario: Upvoting a public recipe moves it ahead in the listing
    Given a public recipe "Lasagna"
    And a public recipe "Soup"
    When "owner-1" upvotes recipe "Lasagna" in the house
    Then the response status is 204
    And recipe "Lasagna" is upvoted by "owner-1"
    When "owner-1" lists recipes in the house
    Then the response status is 200
    And the first recipe in the list is "Lasagna"
    When "owner-1" removes their upvote on recipe "Lasagna" in the house
    Then the response status is 204
    And recipe "Lasagna" is not upvoted by "owner-1"
