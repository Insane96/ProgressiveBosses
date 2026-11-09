# 5.0.0.4-alpha
* The config is now a server config. **`config/progressivebosses-common.toml` is no longer used, options must be set again in `config/progressivebosses/server.toml`**
  * Values are decided by the server and synced to clients, and can be overridden per world in `saves/<world>/serverconfig/`
* The Dragon Egg tooltip now uses InsaneLib's `insanelib:has_tooltip` item tag, the "Egg tooltip" config option has been removed
* Requires InsaneLib 2.5.3.0

# 5.0.0.3-alpha
* Fixed crash with Ice and Fire

# 5.0.0.2-alpha
* Crash fix again

# 5.0.0.1-alpha
* Crash fix and bumped Neo and InsaneLib versions

# 5.0.0.0-alpha
* Port to 1.21.1
* Disabled adventure mode for Elder Guardians