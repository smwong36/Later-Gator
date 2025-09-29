# Team Challenge Tracker

A record of challenges our team faced during the CEN3031 project, how we solved them, and how we can frame them in interviews.

---

## Format
- **Challenge**: What went wrong or got in the way.  
- **Impact**: Why it mattered (to the project or team).  
- **Resolution**: How we solved or adapted.  
- **Interview Framing**: How we’d explain it as a strength.

---

## Challenges

### 1. Environment/Hardware Failure (9/19/25)
- **Challenge**: Sam’s laptop crashed (blue screen of death) → had to switch to a work computer without terminal access.  
- **Impact**: She couldn’t handle local Git, setup access was restricted.  
- **Resolution**: Responsibilities were shifted — Sam managed repo ownership/admin tasks while others handled terminal commands.  
- **Interview Framing**: *“We adapted responsibilities dynamically when a teammate lost development environment access.”*

---

### 2. Repo Setup Without Docs (9/19/25)
- **Challenge**: Repo created before any baseline docs or branches.  
- **Impact**: CircleCI and Jira had nothing to connect to.  
- **Resolution**: Created `main` branch and pushed README to stabilize repo.  
- **Interview Framing**: *“We quickly stabilized the foundation by standardizing a main branch and README before layering in integrations.”*

---

### 3. CircleCI Default Branch Issue (9/19/25)
- **Challenge**: CircleCI auto-generated a `circleci-project-setup` branch.  
- **Impact**: Mismatch with GitHub default branch.  
- **Resolution**: Repo owner switched default branch to `main`.  
- **Interview Framing**: *“We diagnosed CI misalignment and leveraged repo owner privileges to reset configuration.”*

---

### 4. Jira Integration Confusion (9/19/25)
- **Challenge**: Admins couldn’t access Jira’s “Configure” page.  
- **Impact**: Integration with GitHub seemed broken.  
- **Resolution**: Tested Smart Commits → confirmed Jira could link commits to issues.  
- **Interview Framing**: *“We validated partial integration with a Smart Commit test instead of getting stuck.”*

---

### 5. Parallel Branch Development (9/19/25)
- **Challenge**: Danielle updated README in a new branch while others tested Jira commits.  
- **Impact**: Risk of desync and merge conflicts.  
- **Resolution**: Remembered to `git pull main` before pushing changes.  
- **Interview Framing**: *“We reinforced pull-before-push early to manage parallel contributions smoothly.”*

---

### 6. User Story Categorization Awareness (9/27/25)  
- **Challenge**: While drafting user stories, we often struggled with which category a feature belonged in and slipped into describing features from a technical perspective rather than from the user’s experience.  
- **Impact**: This created confusion in how stories were grouped and risked losing the user-centered framing that user stories are meant to emphasize.  
- **Resolution**: The process of refining and categorizing helped us recognize this tendency early and redirect focus back to the user perspective.  
- **Interview Framing**: *“We learned the importance of structuring user stories around the user’s experience rather than technical implementation, and became more intentional about how categorization shapes clarity.”*  

---

## Notes for Future Tracking
- Add new entries as they happen (with date if helpful).  
- Use this file as a reflection tool before presentations or interviews.  
