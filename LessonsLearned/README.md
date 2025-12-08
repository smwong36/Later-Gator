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

### 7. Diagram Design and Structural Clarity (10/16/25)  
- **Challenge**: Designing diagrams proved difficult, as the structure and logic didn’t fully click for everyone at first. Our initial approach with wireframes and a 2D swim lane model led to confusion about how layers and user journeys aligned.
- **Impact**: The lack of clarity slowed development and made it harder to visualize how different parts of the app and technologies interacted within the layered architecture.
- **Resolution**: By stepping back to analyze smaller components individually and referencing our wireframes, notes, and user stories, we reorganized our approach into a more intuitive flowchart of the Later Gator App. This clarified functional connections, prompted discussion of design trade-offs, and led us to reintroduce a business logic layer for greater structure and realism.  
- **Interview Framing**: *“We learned that breaking complex systems into smaller, understandable parts—and iterating on visual models—helped us transform initial confusion into clarity and create a more cohesive architectural design.”*  

---

### 8. Tool Incompatibility & Major Technical Pivot (11/1–11/10)
**Challenge:**: Room + React Native were fundamentally incompatible due to KSP/KAPT generating Kotlin metadata that React Native could not compile. This caused repeated build failures and blocked development.
**Impact:**: This issue stalled progress for several days, increased team frustration, and jeopardized our ability to complete Sprint 2 deliverables.
**Resolution:**: We investigated the root cause across different devices, removed Room entirely, pivoted to a pre-populated SQLite solution, and rebuilt the data layer to restore project stability.
**Interview Framing:**: *“We discovered a toolchain incompatibility that broke our build. After analyzing the root cause, we pivoted to an alternative architecture and restored momentum quickly.”*

---

### 9. Oversized Tasks & Scope Creep
**Challenge:**: Several backlog items were too large, ambiguous, or unclear in scope.
**Impact:**: This made estimation inaccurate, slowed down progress, and caused one blocked task to hold up unrelated areas of development.
**Resolution:**: We broke tasks into smaller, well-defined units, used test cases to clarify requirements, and defined clear boundaries at the moment each task was created.
**Interview Framing:**: *“We shifted to smaller, testable tasks, which improved estimation accuracy and increased our development velocity.”*

---

### 10. Emotional Highs/Lows & Maintaining Morale
**Challenge:**: Retrospectives revealed emotional swings due to ongoing technical setbacks and uncertainty about building a fully functional prototype.
**Impact:**: This created moments of discouragement and increased stress during difficult debugging periods.
**Resolution:**: We incorporated paired programming for complex issues, used timeline visualizations to acknowledge shared struggles, and reinforced a supportive team culture.
**Interview Framing:**: *“We maintained strong morale during difficult periods by supporting each other, collaborating more closely, and normalizing the learning curve.”*

---

### 11. Mid-Sprint Replanning After the Technical Pivot
**Challenge:**: Removing Room invalidated most of the original Sprint 2 plan.
**Impact:**: This rendered several tasks irrelevant, required immediate backlog restructuring, and increased pressure due to approaching deadlines.
**Resolution:**: We rebuilt the backlog around MVP-critical functionality, clarified the new architectural direction, and deprioritized non-essential stretch goals to keep the sprint achievable.
**Interview Framing:**: *“We demonstrated agility by reorganizing the sprint mid-stream and recalibrating our backlog to deliver the most critical pieces despite major changes.”*

---

## Notes for Future Tracking
- Add new entries as they happen (with date if helpful).  
- Use this file as a reflection tool before presentations or interviews.  
