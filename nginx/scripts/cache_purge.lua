local ngx = ngx
local md5 = ngx.md5
local fmt = string.format

-- Function to purge the cache by the key (in this case, the 'date' parameter)
local function purge_cache(cache_key)
    -- Compute MD5 hash of the cache key
    local hash = md5(cache_key)

    -- Build the cache file path based on the hash
    local cache_file = fmt("/var/cache/nginx/%s", hash)

    -- Attempt to remove the cache file
    local file = io.open(cache_file, "r")
    if file then
        file:close()
        os.remove(cache_file)
        ngx.say("Cache purged successfully: " .. cache_key)
    else
        ngx.say("Cache file not found for key: " .. cache_key)
    end
end

-- Get the 'date' parameter from the query string
local cache_key = ngx.var.arg_date
if cache_key then
    purge_cache(cache_key)
else
    ngx.say("No cache key provided.")
end