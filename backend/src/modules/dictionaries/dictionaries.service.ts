import { Injectable } from '@nestjs/common';

@Injectable()
export class DictionariesService {
  list() {
    return {
      success: true,
      data: {
        roles: ['admin', 'doctor', 'research_assistant', 'readonly'],
        genders: ['female', 'male', 'other', 'unknown'],
        caseStatuses: ['draft', 'archived'],
        molecularSubtypes: [
          'luminal_a',
          'luminal_b_her2_negative',
          'luminal_b_her2_positive',
          'her2_positive',
          'triple_negative',
          'unknown',
        ],
        currentStatuses: ['treating', 'follow_up', 'recurrence', 'deceased', 'lost'],
        followUpPlanStatuses: ['pending', 'completed', 'overdue', 'lost'],
        attachmentCategories: [
          'pathology',
          'imaging',
          'laboratory',
          'surgery',
          'discharge_summary',
          'outpatient_record',
          'follow_up',
          'other',
        ],
      },
    };
  }
}
