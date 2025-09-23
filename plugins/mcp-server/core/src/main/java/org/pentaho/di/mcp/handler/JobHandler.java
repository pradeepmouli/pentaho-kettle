/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.mcp.handler;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.www.JobMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles MCP requests for job operations
 */
public class JobHandler {
  
  private final JobMap jobMap;
  private final LogChannelInterface log;
  
  public JobHandler(JobMap jobMap, LogChannelInterface log) {
    this.jobMap = jobMap;
    this.log = log;
  }

  public Object createJob(Map<String, Object> params) throws KettleException {
    String name = (String) params.get("name");
    if (StringUtil.isEmpty(name)) {
      throw new KettleException("Job name is required");
    }
    
    // Create a new job
    JobMeta jobMeta = new JobMeta();
    jobMeta.setName(name);
    
    // Set optional description
    String description = (String) params.get("description");
    if (!StringUtil.isEmpty(description)) {
      jobMeta.setDescription(description);
    }
    
    // Generate unique ID for this job
    String jobId = UUID.randomUUID().toString();
    
    // Create and register the job
    Job job = new Job(null, jobMeta);
    jobMap.addJob(name, jobId, job, null);
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", jobId);
    result.put("name", name);
    result.put("status", "created");
    
    log.logBasic("Created job: " + name + " with ID: " + jobId);
    return result;
  }

  public Object loadJob(Map<String, Object> params) throws KettleException {
    String filename = (String) params.get("filename");
    String name = (String) params.get("name");
    
    if (StringUtil.isEmpty(filename) && StringUtil.isEmpty(name)) {
      throw new KettleException("Either filename or name must be provided");
    }
    
    JobMeta jobMeta;
    if (!StringUtil.isEmpty(filename)) {
      jobMeta = new JobMeta(filename, null);
    } else {
      // Load from repository by name - would need repository implementation
      throw new KettleException("Loading by name from repository not yet implemented");
    }
    
    String jobId = UUID.randomUUID().toString();
    Job job = new Job(null, jobMeta);
    jobMap.addJob(jobMeta.getName(), jobId, job, null);
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", jobId);
    result.put("name", jobMeta.getName());
    result.put("filename", jobMeta.getFilename());
    result.put("status", "loaded");
    
    log.logBasic("Loaded job: " + jobMeta.getName() + " with ID: " + jobId);
    return result;
  }

  public Object saveJob(Map<String, Object> params) throws KettleException {
    String jobId = (String) params.get("id");
    String filename = (String) params.get("filename");
    
    if (StringUtil.isEmpty(jobId)) {
      throw new KettleException("Job ID is required");
    }
    
    Job job = jobMap.getJob(jobId);
    if (job == null) {
      throw new KettleException("Job not found: " + jobId);
    }
    
    if (!StringUtil.isEmpty(filename)) {
      job.getJobMeta().setFilename(filename);
      job.getJobMeta().saveSharedObjects();
      job.getJobMeta().saveToRepository(null);
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", jobId);
    result.put("name", job.getJobMeta().getName());
    result.put("filename", job.getJobMeta().getFilename());
    result.put("status", "saved");
    
    log.logBasic("Saved job: " + job.getJobMeta().getName());
    return result;
  }

  public Object executeJob(Map<String, Object> params) throws KettleException {
    String jobId = (String) params.get("id");
    
    if (StringUtil.isEmpty(jobId)) {
      throw new KettleException("Job ID is required");
    }
    
    Job job = jobMap.getJob(jobId);
    if (job == null) {
      throw new KettleException("Job not found: " + jobId);
    }
    
    // Start the job
    job.start();
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", jobId);
    result.put("name", job.getJobMeta().getName());
    result.put("status", "running");
    
    log.logBasic("Started job: " + job.getJobMeta().getName());
    return result;
  }

  public Object getJobStatus(Map<String, Object> params) throws KettleException {
    String jobId = (String) params.get("id");
    
    if (StringUtil.isEmpty(jobId)) {
      throw new KettleException("Job ID is required");
    }
    
    Job job = jobMap.getJob(jobId);
    if (job == null) {
      throw new KettleException("Job not found: " + jobId);
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", jobId);
    result.put("name", job.getJobMeta().getName());
    result.put("status", job.getStatus());
    result.put("finished", job.isFinished());
    result.put("errors", job.getErrors());
    
    return result;
  }

  public Object stopJob(Map<String, Object> params) throws KettleException {
    String jobId = (String) params.get("id");
    
    if (StringUtil.isEmpty(jobId)) {
      throw new KettleException("Job ID is required");
    }
    
    Job job = jobMap.getJob(jobId);
    if (job == null) {
      throw new KettleException("Job not found: " + jobId);
    }
    
    job.stopAll();
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", jobId);
    result.put("name", job.getJobMeta().getName());
    result.put("status", "stopped");
    
    log.logBasic("Stopped job: " + job.getJobMeta().getName());
    return result;
  }

  public Object listJobs(Map<String, Object> params) {
    return jobMap.getJobNames();
  }

  public Object deleteJob(Map<String, Object> params) throws KettleException {
    String jobId = (String) params.get("id");
    
    if (StringUtil.isEmpty(jobId)) {
      throw new KettleException("Job ID is required");
    }
    
    Job job = jobMap.getJob(jobId);
    if (job == null) {
      throw new KettleException("Job not found: " + jobId);
    }
    
    String name = job.getJobMeta().getName();
    
    // Stop if running
    if (!job.isFinished()) {
      job.stopAll();
    }
    
    // Remove from map
    jobMap.removeJob(jobId);
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", jobId);
    result.put("name", name);
    result.put("status", "deleted");
    
    log.logBasic("Deleted job: " + name);
    return result;
  }
}