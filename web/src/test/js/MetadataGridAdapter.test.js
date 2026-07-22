import { describe, it, expect } from 'vitest';
import { MetadataGridAdapter } from '../../main/webapp/js/util/MetadataGridAdapter.js';

describe('MetadataGridAdapter', () => {
  it('detects 12-column active metadata and scales span correctly', () => {
    const metadata = { gridSystem: '12-column', active: true };

    expect(MetadataGridAdapter.translateLayout(metadata, { span: 3 })).toEqual({
      appearance: 'w1',
    });
    expect(MetadataGridAdapter.translateLayout(metadata, { span: 6 })).toEqual({
      appearance: 'w2',
    });
    expect(MetadataGridAdapter.translateLayout(metadata, { span: 9 })).toEqual({
      appearance: 'w3',
    });
    expect(MetadataGridAdapter.translateLayout(metadata, { span: 12 })).toEqual(
      { appearance: 'w4' }
    );
    expect(MetadataGridAdapter.translateLayout(metadata, { span: 4 })).toEqual({
      appearance: 'w1',
    }); // 4/3 = 1.33 => 1
    expect(MetadataGridAdapter.translateLayout(metadata, { span: 5 })).toEqual({
      appearance: 'w2',
    }); // 5/3 = 1.66 => 2
    expect(MetadataGridAdapter.translateLayout(metadata, { span: 1 })).toEqual({
      appearance: 'w1',
    }); // 1/3 = 0.33 => 1 (clamped)
  });

  it('ignores metadata without active flag or 12-column gridSystem', () => {
    const inactiveMetadata = { gridSystem: '12-column', active: false };
    const wrongSystem = { gridSystem: '24-column', active: true };
    const noMetadata = null;

    expect(
      MetadataGridAdapter.translateLayout(inactiveMetadata, { span: 12 })
    ).toEqual({ appearance: '' });
    expect(
      MetadataGridAdapter.translateLayout(wrongSystem, { span: 12 })
    ).toEqual({ appearance: '' });
    expect(
      MetadataGridAdapter.translateLayout(noMetadata, { span: 12 })
    ).toEqual({ appearance: '' });
  });

  it('uses default heuristics when grid is not 12-column active or span is missing', () => {
    const inactiveMetadata = { gridSystem: '12-column', active: false };

    expect(
      MetadataGridAdapter.translateLayout(inactiveMetadata, {
        span: 12,
        defaultAppearance: 'w2',
      })
    ).toEqual({ appearance: 'w2' });

    expect(
      MetadataGridAdapter.translateLayout(
        { gridSystem: '12-column', active: true },
        { defaultAppearance: 'w3' }
      )
    ).toEqual({ appearance: 'w3' });
  });
});
