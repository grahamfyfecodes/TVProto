# ShowTrack — Data Sync Specification

Working document. Decisions marked with **[TBD]** are open.

---

## Entity Overview

```
┌──────────────┐       ┌──────────────────┐
│    Show       │──1:N──│    Episode        │
│              │       │                  │
│ id (PK)      │       │ id (PK)          │
│ name         │       │ showId (FK)      │
│ imageUrl     │       │ season           │
│ status       │       │ number           │
│ networkName  │       │ name             │
│ webChannelName│      │ airdate          │
│ scheduleTime │       │ airtime          │
│ scheduleDays │       │ watched          │
│ lastUpdated  │       │ lastUpdated      │
└──────────────┘       └──────────────────┘
```

---

## Field Catalogue — Show

| Field            | Source                     | When Populated         | Persistence | Freshness Strategy                    |
|------------------|----------------------------|------------------------|-------------|---------------------------------------|
| id               | TVmaze search              | On track               | Permanent   | Never changes                         |
| name             | TVmaze search              | On track               | Permanent   | Rarely changes; refresh on app launch |
| imageUrl         | TVmaze search              | On track               | Stable      | Refresh on app launch                 |
| status           | TVmaze search              | On track               | Volatile    | Show can end/renew; refresh on launch |
| networkName      | TVmaze search (show.network.name) | On track        | Stable      | Rarely changes                        |
| webChannelName   | TVmaze search (show.webchannel.name) | On track     | Stable      | Rarely changes                        |
| scheduleTime     | TVmaze search (show.schedule.time) | On track        | Stable      | Can shift between seasons             |
| scheduleDays     | TVmaze search (show.schedule.days) | On track        | Stable      | Can shift between seasons             |
| lastUpdated      | Device clock               | On any API refresh     | Internal    | Updated automatically                 |

## Field Catalogue — Episode

| Field        | Source                              | When Populated                   | Persistence | Freshness Strategy                          |
|--------------|-------------------------------------|----------------------------------|-------------|---------------------------------------------|
| id           | TVmaze episodes endpoint            | On track (bulk fetch)            | Permanent   | Never changes                               |
| showId       | Derived from parent show            | On track                         | Permanent   | Never changes                               |
| season       | TVmaze episodes endpoint            | On track                         | Permanent   | Never changes                               |
| number       | TVmaze episodes endpoint            | On track                         | Permanent   | Never changes                               |
| name         | TVmaze episodes endpoint            | On track                         | Stable      | TBA titles resolve later; refresh on launch  |
| airdate      | TVmaze episodes endpoint            | On track                         | Volatile    | Future dates can shift; refresh periodically |
| airtime      | TVmaze nextepisode embed (Option 2) | On upcoming tab / background sync| Volatile    | Only accurate close to air; re-fetch when online |
| watched      | User action (local only)            | On tap                           | Permanent   | Never sourced from API; survives all syncs   |
| lastUpdated  | Device clock                        | On any API refresh               | Internal    | Updated automatically                        |

---

## Data Lifecycle

### On Track (user adds a show)

1. Show metadata saved to Room (including network, schedule)
2. Full episode list fetched via `/shows/{id}/episodes` and saved
3. `airtime` is **null** for all episodes at this point — bulk endpoint doesn't include it

### On Upcoming Tab Visit (online)

1. For each tracked show with status = "Running", hit `/shows/{id}?embed=nextepisode`
2. If `_embedded.nextepisode` exists, update that episode's `airtime` in Room
3. If new episodes exist that we don't have locally, insert them
4. Update `lastUpdated` on the show

### On Upcoming Tab Visit (offline)

1. Query Room for episodes with `airdate` between today and today + UPCOMING_DAYS_AHEAD
2. Display `airtime` if available, fall back to show's `scheduleTime` if not
3. Display `networkName` / `webChannelName` from the Show entity
4. Show `lastUpdated` so Lisa knows data may be stale
5. If `airdate` is in the past and `watched` is false, flag as "may have aired"

### Background Refresh **[TBD — WorkManager scope]**

1. Re-fetch episode list for tracked shows with status = "Running"
2. Re-fetch nextepisode embed for upcoming shows
3. Frequency: **[TBD]** — daily? on app launch only?
4. Respect TVmaze rate limit (20 calls / 10 seconds)

---

## Staleness Scenarios

| Scenario                                    | Impact                                        | Mitigation                                            |
|---------------------------------------------|-----------------------------------------------|-------------------------------------------------------|
| Show cancelled between syncs                | Status still shows "Running"                  | Refresh show metadata on launch                       |
| Episode date shifted                        | Old airdate shown in upcoming                 | Periodic episode list re-fetch                        |
| New season announced                        | Episodes missing from local DB                | Re-fetch episode list on launch / background          |
| Lisa offline for 2+ weeks                   | Nextepisode data points to past episode       | Compare airdate to device clock; flag stale entries   |
| Lisa watched on TV but didn't mark in app   | Episode still shows as "upcoming"             | Can't solve without external data; accept limitation  |

---

## Offline Behaviour Summary

| Feature          | Offline Capability          | Data Source         | Limitation                        |
|------------------|-----------------------------|---------------------|-----------------------------------|
| Search           | Not available               | API only            | Show cached results? **[TBD]**    |
| Tracked Shows    | Fully available             | Room                | Show metadata may be stale        |
| Episode List     | Fully available             | Room                | New episodes won't appear         |
| Mark Watched     | Fully available             | Room (local write)  | None                              |
| Upcoming         | Available with caveats      | Room + device clock | airtime may be missing; dates may have shifted |
| Network/Channel  | Available                   | Room (Show entity)  | None — populated on track         |

---

## Open Decisions

- **[TBD]** Background refresh frequency and triggers
- **[TBD]** `scheduleDays` storage format — store as comma-separated string or introduce a small lookup? Leaning string for simplicity.
- **[TBD]** Should search results be cached for offline browsing, or is search online-only?
- **[TBD]** How to handle shows that move networks between seasons (edge case, likely just overwrite on refresh)
- **[TBD]** Notification strategy — ties into WorkManager and refresh schedule, not yet scoped
