--
-- Created by IntelliJ IDEA.
-- User: dell
-- Date: 21/03/01
-- Time: 下午 11:14
-- To change this template use File | Settings | File Templates.
--

local listKey = 'GlbTR:' .. KEYS[1]
local registerTable = {}
local originalList = redis.call('lrange', listKey, 0, -1)
for i = 1, #originalList do
    registerTable[originalList[i]] = i
end
local newTargetList = {}
local resultList = {}
local originalLength = #originalList
local count = 0
for i = 1, #ARGV do
    local registerCode = registerTable[ARGV[i]]
    if (registerCode == nil) then
        count = count + 1
        table.insert(newTargetList, ARGV[i])
        table.insert(resultList, { ARGV[i], originalLength + count })
    else
        table.insert(resultList, { ARGV[i], registerCode })
    end
end
if (#newTargetList > 0) then
    redis.call('rpush', listKey, unpack(newTargetList))
end
return resultList