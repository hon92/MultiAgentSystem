/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Honza
 */
public class AgentDb
{
    private List<AgentEntry> agentDb;
    private class AgentEntry
    {

        public String address;
        public int port;

        public AgentEntry(String address, int port)
        {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj != null && obj instanceof AgentEntry)
            {
                AgentEntry a = (AgentEntry) obj;
                return a.port == port && a.address.equals(address);
            }
            return false;
        }

    }

    public AgentDb()
    {
        agentDb = new ArrayList<>();
    }

    public boolean addAgent(String address, int port)
    {
        AgentEntry newAgent = new AgentEntry(address, port);
        if (!agentDb.contains(newAgent))
        {
            agentDb.add(newAgent);
            return true;
        }
        return false;
    }

    public List<String> getAgentsList()
    {
        List<String> agents = new ArrayList<>();
        for (AgentEntry ae : agentDb)
        {
            agents.add(String.format("%s:%d", ae.address, ae.port));
        }
        return agents;
    }
}
