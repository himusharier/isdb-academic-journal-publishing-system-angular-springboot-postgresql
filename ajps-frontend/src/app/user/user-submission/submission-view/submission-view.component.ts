import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/internal/Subscription';

declare const bootstrap: any;

@Component({
  selector: 'app-submission-view',
  imports: [CommonModule, FormsModule],
  templateUrl: './submission-view.component.html',
  styleUrl: '../user-submission.component.css'
})
export class SubmissionViewComponent implements OnInit, AfterViewInit, OnDestroy {
  
  manuscript = {
    id: 'MS12345',
    title: 'Monitoring the Seasonal Distribution and Variation of Sea Surface Temperature and Chlorophyll Concentration in Bay of Bengal using MODIS Satellite Images',
    journalName: 'Journal of Modern Science',
    submissionDate: new Date('2025-05-01'),
    author: 'Dr. Jane Smith',
    abstract: 'This paper explores recent advancements in quantum computing technologies...',
    files: [
      { name: 'manuscript.pdf', url: '#', size: '1024' },
      { name: 'supplementary.docx', url: '#', size: '512' }
    ],
    status: {
      submission: 'Submitted',
      review: 'Under Review',
      copyEditing: 'In Progress',
      production: 'In Progress',
      publication: 'Not Published'
    },
    review: {
      startDate: new Date('2025-05-10'),
      reviewers: [
        { name: 'Reviewer 1', status: 'In Progress', comments: 'Needs clarification...' },
        { name: 'Reviewer 2', status: 'Pending', comments: '' }
      ],
      decision: ''
    },
    copyEditing: {
      startDate: new Date('2025-06-01'),
      editor: 'Emily Johnson',
      changes: [
        { description: 'Revise section 2.3 for clarity', status: 'Pending' },
        { description: 'Correct figure 4 caption', status: 'Completed' }
      ]
    },
    production: {
      startDate: new Date('2025-06-10'),
      typesetting: 'In Progress',
      proofs: [
        { name: 'first_proof.pdf', url: '#', sentDate: new Date('2025-06-12') },
        { name: 'second_proof.pdf', url: '#', sentDate: new Date('2025-06-15') }
      ]
    },
    publication: {
      status: 'Not Published',
      date: null,
      doi: '',
      volumeIssue: '',
      accessType: 'Open Access',
      url: ''
    },
    discussions: [
      {
        name: 'IRN/MHSJ A message regarding journal of Research in Nursing, Midwifery and Health Sciences',
        from: 'lighton',
        lastReply: new Date('2022-08-01'),
        replies: 0,
        closed: true
      }
    ]
  };

  private fragmentSubscription: Subscription | undefined;

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    this.fragmentSubscription = this.route.fragment.subscribe(fragment => {
      this.handleFragment(fragment);
    });
  }

  ngAfterViewInit(): void {
    const tabs = document.querySelectorAll('#mainTabs .nav-link');
    tabs.forEach(tab => {
      tab.addEventListener('shown.bs.tab', (event) => {
        const target = (event.target as HTMLElement).getAttribute('href')?.substring(1);
        this.router.navigate([], { fragment: target, relativeTo: this.route });
      });
    });
  }

  ngOnDestroy(): void {
    if (this.fragmentSubscription) {
      this.fragmentSubscription.unsubscribe();
    }
  }

  private handleFragment(fragment: string | null): void {
    const validTabs = ['submission', 'review', 'copyediting', 'production', 'publication'];
    const tabToActivate = fragment && validTabs.includes(fragment) ? fragment : 'submission';
    this.activateTab(tabToActivate);
  }

  private activateTab(tab: string): void {
    const tabElement = document.querySelector(`#${tab}-tab`) as HTMLElement;
    if (tabElement) {
      const tabInstance = new bootstrap.Tab(tabElement);
      tabInstance.show();
    }
  }

  uploadFile(): void {
    // Placeholder for file upload logic
    alert('File upload functionality to be implemented');
  }

  downloadAllFiles(): void {
    // Placeholder for download all files logic
    alert('Download all files functionality to be implemented');
  }

  addDiscussion(): void {
    // Placeholder for add discussion logic
    alert('Add discussion functionality to be implemented');
  }

  sendToReview(): void {
    alert('Send to Review functionality to be implemented');
  }

  acceptAndSkipReview(): void {
    alert('Accept and Skip Review functionality to be implemented');
  }

  declineSubmission(): void {
    alert('Decline Submission functionality to be implemented');
  }

  assignParticipant(): void {
    alert('Assign Participant functionality to be implemented');
  }

  downloadFile(fileUrl: string, fileName: string): void {
  const link = document.createElement('a');
  link.href = fileUrl;
  link.download = fileName;
  link.target = '_blank'; // Optional: opens in new tab
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

}