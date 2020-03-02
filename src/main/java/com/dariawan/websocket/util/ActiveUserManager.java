/**
 * Spring Boot + WebSocket Examples  (https://www.dariawan.com)
 * Copyright (C) 2020 Dariawan <hello@dariawan.com>
 *
 * Creative Commons Attribution-ShareAlike 4.0 International License
 *
 * Under this license, you are free to: # Share - copy and redistribute the
 * material in any medium or format # Adapt - remix, transform, and build upon
 * the material for any purpose, even commercially.
 *
 * The licensor cannot revoke these freedoms as long as you follow the license
 * terms.
 *
 * License terms: # Attribution - You must give appropriate credit, provide a
 * link to the license, and indicate if changes were made. You may do so in any
 * reasonable manner, but not in any way that suggests the licensor endorses you
 * or your use. # ShareAlike - If you remix, transform, or build upon the
 * material, you must distribute your contributions under the same license as
 * the original. # No additional restrictions - You may not apply legal terms or
 * technological measures that legally restrict others from doing anything the
 * license permits.
 *
 * Notices: # You do not have to comply with the license for elements of the
 * material in the public domain or where your use is permitted by an applicable
 * exception or limitation. # No warranties are given. The license may not give
 * you all of the permissions necessary for your intended use. For example,
 * other rights such as publicity, privacy, or moral rights may limit how you
 * use the material.
 *
 * You may obtain a copy of the License at
 * https://creativecommons.org/licenses/by-sa/4.0/
 * https://creativecommons.org/licenses/by-sa/4.0/legalcode
 */
package com.dariawan.websocket.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class ActiveUserManager {

    private final Map<String, Object> map;

    private final List<ActiveUserChangeListener> listeners;

    private final ThreadPoolExecutor notifyPool;

    private ActiveUserManager() {
        map = new ConcurrentHashMap<>();
        listeners = new CopyOnWriteArrayList<>();
        notifyPool = new ThreadPoolExecutor(1, 5, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
    }

    public void add(String userName, String remoteAddress) {
        map.put(userName, remoteAddress);
        notifyListeners();
    }

    /**
     * Removes username from the concurrentHashMap.
     *
     * @param username - to be removed
     */
    public void remove(String username) {
        map.remove(username);
        notifyListeners();
    }

    /**
     * Get all active user
     *
     * @return - Set of active users.
     */
    public Set<String> getAll() {
        return map.keySet();
    }

    /**
     * Get a set of all active user except username that passed in the parameter
     *
     * @param username - current username
     * @return - set of users except passed username
     */
    public Set<String> getActiveUsersExceptCurrentUser(String username) {
        Set<String> users = new HashSet<>(map.keySet());
        users.remove(username);
        return users;
    }
    
    /**
     * To get notified when active users changed
     *
     * @param listener - object that implements ActiveUserChangeListener
     */
    public void registerListener(ActiveUserChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Stop receiving notification.
     *
     * @param listener - object that implements ActiveUserChangeListener
     */
    public void removeListener(ActiveUserChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        notifyPool.submit(() -> listeners.forEach(ActiveUserChangeListener::notifyActiveUserChange));
    }
}
