import React, { useEffect, useState, useRef, useCallback } from 'react';
import { store } from '../store';
import { THEME } from '../theme';
import { useAccessibility } from './AccessibilityProvider.jsx';

const schema = {
  formOID: 'F_TEST_1',
  groups: [
    {
      groupOID: 'IG_NON_REP',
      repeating: false,
      title: 'General Info',
      fields: [
        {
          fieldOID: 'I_GEN_NOTES',
          label: 'Notes',
          type: 'text',
        },
      ],
    },
    {
      groupOID: 'IG_AE_1',
      repeating: true,
      title: 'Adverse Events',
      fields: [
        {
          fieldOID: 'I_AE_TERM',
          label: 'Adverse Event Term',
          type: 'text',
        },
        {
          fieldOID: 'I_AE_SEVERITY',
          label: 'Severity',
          type: 'select',
          options: ['Mild', 'Moderate', 'Severe'],
        },
        {
          fieldOID: 'I_AE_ONSET',
          label: 'Onset Date',
          type: 'date',
        },
      ],
    },
  ],
};

const getFieldDiscrepancy = (fieldId, value) => {
  if (fieldId === "IG_NON_REP[0].I_GEN_NOTES") {
    if (!value || value.trim() === '') {
      return {
        severityCode: "ERR_01",
        badgeClass: "alert",
        text: "Note cannot be empty."
      };
    }
  }
  if (fieldId === "IG_AE_1[0].I_AE_ONSET") {
    if (value) {
      const date = new Date(value);
      if (date > new Date()) {
        return {
          severityCode: "WARN_01",
          badgeClass: "alertbox_center",
          text: "Date is in the future. Please verify."
        };
      }
    }
  }
  return null;
};

const FormField = React.memo(function FormField({ field, fieldId, value, groupOID, index }) {
  const [localValue, setLocalValue] = useState(value || '');
  const { announce } = useAccessibility();

  // Sync local value when global value changes externally
  useEffect(() => {
    setLocalValue(value || '');
  }, [value]);

  const handleChange = useCallback((e) => {
    setLocalValue(e.target.value);
  }, []);

  const handleBlur = useCallback(() => {
    if (localValue !== (value || '')) {
      store.setFormData(groupOID, index, field.fieldOID, localValue);
    }
  }, [localValue, value, groupOID, index, field.fieldOID]);

  const discrepancy = getFieldDiscrepancy(fieldId, value);
  const discrepancyId = discrepancy ? `${fieldId}-discrepancy` : undefined;

  const prevDiscrepancy = useRef(discrepancy);
  useEffect(() => {
    const prev = prevDiscrepancy.current;
    if (discrepancy && (!prev || prev.text !== discrepancy.text)) {
      announce(`Discrepancy on ${field.label}: ${discrepancy.text}`);
    } else if (!discrepancy && prev) {
      announce(`Discrepancy cleared for ${field.label}`);
    }
    prevDiscrepancy.current = discrepancy;
  }, [discrepancy, announce, field.label]);

  return (
    <div style={{ marginBottom: '10px' }}>
      <label
        htmlFor={fieldId}
        style={{ display: 'inline-block', width: '150px' }}
      >
        {field.label}:
      </label>
      
      {discrepancy && (
        <span 
          className={`discrepancy-badge ${discrepancy.badgeClass}`} 
          style={{ marginLeft: '5px', marginRight: '5px', fontWeight: 'bold', color: 'red' }}
          title={`Severity: ${discrepancy.severityCode}`}
        >
          [{discrepancy.severityCode}]
        </span>
      )}
      {field.type === 'select' ? (
        <select
          id={fieldId}
          name={fieldId}
          value={localValue}
          onChange={handleChange}
          onBlur={handleBlur}
          aria-describedby={discrepancyId}
          aria-invalid={discrepancy ? 'true' : 'false'}
        >
          <option value="">--Select--</option>
          {field.options.map((opt) => (
            <option key={opt} value={opt}>
              {opt}
            </option>
          ))}
        </select>
      ) : (
        <input
          id={fieldId}
          name={fieldId}
          type={field.type}
          value={localValue}
          onChange={handleChange}
          onBlur={handleBlur}
          aria-describedby={discrepancyId}
          aria-invalid={discrepancy ? 'true' : 'false'}
        />
      )}

      {discrepancy && (
        <div 
          id={discrepancyId} 
          className="sr-only"
        >
          {discrepancy.text}
        </div>
      )}
    </div>
  );
});

const FormRow = React.memo(function FormRow({ group, row, index }) {
  const { announce } = useAccessibility();

  return (
    <div
      style={{
        marginBottom: '15px',
        padding: '10px',
        backgroundColor: '#f9f9f9',
      }}
    >
      {group.repeating && <h4>Row {index + 1}</h4>}
      {group.fields.map((field) => {
        const fieldId = `${group.groupOID}[${index}].${field.fieldOID}`;
        const value = row[field.fieldOID];

        return (
          <FormField
            key={field.fieldOID}
            field={field}
            fieldId={fieldId}
            value={value}
            groupOID={group.groupOID}
            index={index}
          />
        );
      })}
      {group.repeating && (
        <button
          type="button"
          onClick={() => {
            store.removeRow(group.groupOID, index);
            announce(`Row ${index + 1} removed from ${group.title}`);
          }}
        >
          Remove Row
        </button>
      )}
    </div>
  );
});

const FormGroup = React.memo(function FormGroup({ group, rows }) {
  const { announce } = useAccessibility();

  return (
    <div
      style={{
        marginTop: '20px',
        border: `1px solid ${THEME.colors.border}`,
        padding: '10px',
      }}
    >
      <h3>{group.title}</h3>
      {rows.map((row, index) => (
        <FormRow
          key={index}
          group={group}
          row={row}
          index={index}
        />
      ))}
      {group.repeating && (
        <button
          type="button"
          onClick={() => {
            store.addRow(group.groupOID, schema);
            announce(`New row added to ${group.title}`);
          }}
        >
          Add {group.title} Entry
        </button>
      )}
    </div>
  );
});

export default function CRFRenderer() {
  const [studyOID, setStudyOID] = useState(store.getState().studyOID);
  const [formData, setFormData] = useState(store.getState().formData);
  const [loading, setLoading] = useState(true);
  const { announce } = useAccessibility();

  useEffect(() => {
    announce('Form loading started');
    // Initialize default row for repeating groups if empty
    schema.groups.forEach((group) => {
      const data = store.getState().formData[group.groupOID];
      if (!data || data.length === 0) {
        if (!group.repeating) {
          store.addRow(group.groupOID, schema);
        }
      }
    });

    const timer = setTimeout(() => {
      setLoading(false);
      announce('Form loading completed');
    }, 500);

    const unsubscribe = store.subscribe((state) => {
      setStudyOID(state.studyOID);
      setFormData(state.formData);
    });

    return () => {
      clearTimeout(timer);
      unsubscribe();
    };
  }, []); 

  if (loading) {
    return <div className="spinner">Loading CRF Data...</div>;
  }

  return (
    <div
      className="crf-renderer"
      style={{ padding: '20px', fontFamily: 'sans-serif' }}
    >
      <h1>Printable CRF View</h1>
      <div className="crf-details">
        <h2>Study Details</h2>
        <p>
          <strong>Study OID:</strong> {studyOID}
        </p>
        <p>
          This is a modernized reactive rendering of the Case Report Form (CRF).
        </p>
      </div>

      <form onSubmit={(e) => e.preventDefault()}>
        {schema.groups.map((group) => {
          const rows = formData[group.groupOID] || [];
          return <FormGroup key={group.groupOID} group={group} rows={rows} />;
        })}
      </form>

      {/* Investigator Signature Block */}
      <div
        className="investigator-signature"
        style={{
          marginTop: '40px',
          borderTop: '1px solid #000',
          paddingTop: '10px',
        }}
      >
        <p>
          <strong>{window.app_investigatorLabel || 'Investigator'}:</strong>{' '}
          _________________________
        </p>
        <p>
          <strong>
            {window.app_investigatorSignatureLabel || 'Investigator Signature'}:
          </strong>{' '}
          _________________________
        </p>
        <p>
          <em>
            {window.app_meaning_of_signatureLabel || 'Meaning of Signature'}:
          </em>{' '}
          I attest that the data is accurate.
        </p>
      </div>
    </div>
  );
}
