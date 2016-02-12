/*
 * SonarQube :: GitHub Plugin
 * Copyright (C) 2015 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.github;

import javax.annotation.Nullable;
import org.kohsuke.github.GHCommitState;
import org.sonar.api.issue.Issue;
import org.sonar.api.rule.Severity;

public class GlobalReport {
  private int[] newIssuesBySeverity = new int[Severity.ALL.size()];

  private void increment(String severity) {
    this.newIssuesBySeverity[Severity.ALL.indexOf(severity)]++;
  }

  public String getStatusDescription() {
    StringBuilder sb = new StringBuilder();
    printNewIssuesInline(sb);
    return sb.toString();
  }

  public GHCommitState getStatus() {
    return newIssues(Severity.BLOCKER) > 0 ||
           newIssues(Severity.CRITICAL) > 0 ||
           newIssues(Severity.MAJOR) > 0 ||
           newIssues(Severity.MINOR) > 0 ? GHCommitState.ERROR : GHCommitState.SUCCESS;
  }

  private int newIssues(String s) {
    return newIssuesBySeverity[Severity.ALL.indexOf(s)];
  }

  private void printNewIssuesInline(StringBuilder sb) {
    sb.append("SonarQube reported ");
    int newIssues = newIssues(Severity.BLOCKER) + newIssues(Severity.CRITICAL) + newIssues(Severity.MAJOR) + newIssues(Severity.MINOR);
    if (newIssues > 0) {
      sb.append(newIssues).append(" issue" + (newIssues > 1 ? "s" : "")).append(",");
      if (newIssues(Severity.BLOCKER) > 0) {
        printNewIssuesInline(sb, Severity.BLOCKER);
      }
      if (newIssues(Severity.CRITICAL) > 0) {
        printNewIssuesInline(sb, Severity.CRITICAL);
      }
      if (newIssues(Severity.MAJOR) > 0) {
        printNewIssuesInline(sb, Severity.MAJOR);
      }
      if (newIssues(Severity.MINOR) > 0) {
        printNewIssuesInline(sb, Severity.MINOR);
      }
    } else {
      sb.append("no issues");
    }
  }

  private void printNewIssuesInline(StringBuilder sb, String severity) {
    int issueCount = newIssues(severity);
    if (issueCount > 0) {
      if (sb.charAt(sb.length() - 1) == ',') {
        sb.append(" with ");
      } else {
        sb.append(" and ");
      }
      sb.append(issueCount).append(" ").append(severity.toLowerCase());
    }
  }

  public void process(Issue issue, @Nullable String githubUrl, boolean reportedOnDiff) {
    if (reportedOnDiff) {
      increment(issue.severity());
    }
  }
}
