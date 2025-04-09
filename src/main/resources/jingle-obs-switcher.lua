obs = obslua

---- Variables ----
jingle_dir = os.getenv("UserProfile"):gsub("\\", "/") .. "/.config/Jingle/"
last_scene_state = ''

---- File Functions ----
function read_first_line(filename)
    local rfile = io.open(filename, "r")
    if rfile == nil then
        return ""
    end
    io.input(rfile)
    local out = io.read()
    io.close(rfile)
    return out
end

function get_state_file_string(filename)
    local success, result = pcall(read_first_line, jingle_dir .. filename)
    if success then
        return result
    end
    return nil
end

---- Misc Functions ----
function split_string(input_string, split_char)
    local out = {}
    for str in input_string.gmatch(input_string, "([^" .. split_char .. "]+)") do
        table.insert(out, str)
    end
    return out
end

---- Obs Functions ----
function get_scene(name)
    local source = get_source(name)
    if source == nil then
        return nil
    end
    local scene = obs.obs_scene_from_source(source)
    release_source(source)
    return scene
end

function get_source(name)
    return obs.obs_get_source_by_name(name)
end

function release_source(source)
    obs.obs_source_release(source)
end

function switch_to_scene(scene_name)
    print(scene_name)
    local scene_source = get_source(scene_name)
    print(scene_source)
    if (scene_source == nil) then return false end
    obs.obs_frontend_set_current_scene(scene_source)
    release_source(scene_source)
    return true
end

function set_item_visible(scene_name, item_name, visible)
    local scene = get_scene(scene_name)
    if (scene == nil) then
        return
    end
    local item = obs.obs_scene_find_source_recursive(scene, item_name)
    if (item == nil) then
        return
    end
    print("setting " .. scene_name .. ":" .. item_name .. " visibility: " .. tostring(visible))
    obs.obs_sceneitem_set_visible(item, visible)
end

---- Script Functions ----
function script_description()
    return [[
    <h1>Jingle OBS Switcher</h1>
    <p>Links OBS to Jingle for scene switching.</p>
    ]]
end

function script_load()
    last_scene_state = get_state_file_string("obs-switcher-state")
end

function script_update(_)
    if not timers_activated then
        timers_activated = true
        obs.timer_add(loop, 20)
    end
end

function toggle_sources(sources, toggle)
    sources = split_string(sources, "=")
    if (sources[2] ~= nil) then
        sources[2] = split_string(sources[2], "&")
        for _, source in ipairs(sources[2]) do
            local source_data = split_string(source, ':')
            set_item_visible(source_data[1], source_data[2], toggle)
        end
    end
end

function loop()
    local link_state = get_state_file_string("obs-link-state")
    local scene_state = get_state_file_string("obs-switcher-state")
    if (scene_state == nil or scene_state == last_scene_state or split_string(link_state, '|')[1] == 'W') then
        return
    end

    local last_state_args = split_string(last_scene_state, '|')
    local new_state_args = split_string(scene_state, '|')

    toggle_sources(new_state_args[3], true)
    toggle_sources(new_state_args[4], false)

    if (last_state_args[2] ~= new_state_args[2]) then
        switch_to_scene(split_string(new_state_args[2], "=")[2])
    end

    last_scene_state = scene_state
end